/**
 * Script de Simulação de Ônibus em Movimento
 *
 * Busca as paradas reais da enquete aberta (na mesma ordem exibida no mapa)
 * e faz o ônibus passar por cada uma delas, em sequência.
 *
 * Uso:
 *   node simulate_bus.mjs
 */
import { Client } from './frontend/node_modules/@stomp/stompjs/esm6/index.js';

const BACKEND_URL = 'http://localhost:8089';
const WS_URL = 'ws://localhost:8089/stomp/websocket';

const DRIVER_CREDENTIALS = {
  email: 'motorista.carlos@acaminho.com',
  password: '12345678'
};

// Usado apenas para consultar as paradas da enquete aberta
const STUDENT_CREDENTIALS = {
  email: 'estudante.lucas@acaminho.com',
  password: '12345678'
};

const BUS_ID = 'f0000000-0000-0000-0000-000000000001';
const STEPS_BETWEEN_STOPS = 8;
const INTERVAL_MS = 1500;

async function login(credentials) {
  const res = await fetch(`${BACKEND_URL}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(credentials)
  });
  if (!res.ok) {
    throw new Error(`Falha no login (${credentials.email}): ${res.status} ${await res.text()}`);
  }
  return res.json();
}

async function getJson(path, token) {
  const res = await fetch(`${BACKEND_URL}/api${path}`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  if (!res.ok) {
    throw new Error(`Falha em GET ${path}: ${res.status} ${await res.text()}`);
  }
  return res.json();
}

// Paradas da enquete aberta, na ordem em que aparecem no mapa
async function loadStops() {
  const student = await login(STUDENT_CREDENTIALS);
  const polls = await getJson('/polls/open', student.accessToken);
  if (!polls.length) {
    throw new Error('Nenhuma enquete aberta encontrada para hoje.');
  }

  const pollId = process.argv[2] || polls[0].pollId;
  const poll = await getJson(`/polls/${pollId}`, student.accessToken);
  const stops = (poll.options || [])
    .filter((o) => o.latitude != null && o.longitude != null)
    .map((o) => ({ lat: o.latitude, lng: o.longitude, name: o.stopName }));

  if (stops.length < 2) {
    throw new Error('A enquete precisa de ao menos 2 paradas com coordenadas.');
  }

  console.log(`Rota: ${poll.routeName}`);
  stops.forEach((s, i) => console.log(`  Parada ${i + 1}: ${s.name} (${s.lat}, ${s.lng})`));
  return stops;
}

// Interpola entre as paradas; o ônibus passa exatamente sobre cada parada
function buildRoute(stops) {
  const route = [];
  for (let i = 0; i < stops.length - 1; i++) {
    const a = stops[i];
    const b = stops[i + 1];
    for (let step = 0; step < STEPS_BETWEEN_STOPS; step++) {
      const r = step / STEPS_BETWEEN_STOPS;
      route.push({
        lat: Number((a.lat + (b.lat - a.lat) * r).toFixed(6)),
        lng: Number((a.lng + (b.lng - a.lng) * r).toFixed(6)),
        info: step === 0 ? `Parada ${i + 1}: ${a.name}` : `a caminho da parada ${i + 2}`
      });
    }
  }
  const last = stops[stops.length - 1];
  route.push({ lat: last.lat, lng: last.lng, info: `Parada ${stops.length}: ${last.name}` });
  return route;
}

async function main() {
  try {
    console.log('1. Buscando paradas da enquete aberta...');
    const stops = await loadStops();
    const route = buildRoute(stops);

    console.log('\n2. Autenticando motorista no backend...');
    const driver = await login(DRIVER_CREDENTIALS);
    console.log(`✓ Motorista autenticado: ${driver.user.name} (${driver.user.email})`);

    console.log(`\n3. Conectando ao WebSocket STOMP em ${WS_URL}...`);
    const stompClient = new Client({
      brokerURL: WS_URL,
      connectHeaders: { Authorization: `Bearer ${driver.accessToken}` },
      reconnectDelay: 3000,
      debug: () => {}
    });

    stompClient.onConnect = () => {
      console.log('✓ Conexão WebSocket STOMP estabelecida!');
      console.log(`4. Simulando trajeto (${route.length} pontos). Abra http://localhost:4200/student/tracking\n`);

      let index = 0;
      const intervalId = setInterval(() => {
        if (!stompClient.connected) return;

        const point = route[index];
        stompClient.publish({
          destination: '/app/location',
          body: JSON.stringify({ busId: BUS_ID, latitude: point.lat, longitude: point.lng })
        });

        const time = new Date().toLocaleTimeString('pt-BR');
        console.log(`[${time}] (${index + 1}/${route.length}) ${point.lat}, ${point.lng} - ${point.info}`);

        // Ao chegar na última parada, recomeça da primeira (volta ao ponto de partida)
        index = (index + 1) % route.length;
      }, INTERVAL_MS);

      process.on('SIGINT', () => {
        clearInterval(intervalId);
        stompClient.deactivate();
        console.log('\nSimulação encerrada.');
        process.exit(0);
      });
    };

    stompClient.onStompError = (frame) => {
      console.error('Erro no STOMP:', frame.headers['message'], frame.body);
    };

    stompClient.activate();
  } catch (error) {
    console.error('Erro na simulação:', error.message);
  }
}

main();
