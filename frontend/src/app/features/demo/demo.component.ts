import { Component, OnInit } from '@angular/core';
import { LeafletModule } from '@bluehalo/ngx-leaflet';
import * as L from 'leaflet';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-demo',
  imports: [LeafletModule, RouterLink],
  template: `
    <div class="min-h-screen bg-base-200 text-base-content flex flex-col">
      <header class="navbar bg-base-100 shadow-sm px-4 md:px-8">
        <div class="flex-1">
          <a class="btn btn-ghost text-xl font-bold text-primary flex items-center gap-2">
            <svg class="w-6 h-6 text-primary" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 6a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6z" />
              <path d="M4 11h16" />
              <circle cx="8" cy="15" r="1" fill="currentColor" />
              <circle cx="16" cy="15" r="1" fill="currentColor" />
              <path d="M6 18v2" />
              <path d="M18 18v2" />
            </svg>
            <span>A Caminho</span>
          </a>
          <span class="badge badge-primary badge-outline ml-2">Leaflet Demo</span>
        </div>
        <div class="flex-none gap-2">
          <a routerLink="/login" class="btn btn-sm btn-primary">Ir para Login</a>
        </div>
      </header>
      <main class="flex-1 container mx-auto px-4 py-8 flex flex-col items-center justify-center">
        <div class="card w-full max-w-3xl bg-base-100 shadow-xl border border-base-300">
          <div class="card-body text-center items-center">
            <h1 class="card-title text-2xl font-extrabold text-neutral flex items-center justify-center gap-2">
              <svg class="w-6 h-6 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
              </svg>
              <span>Mapa Interativo Leaflet (BlueHalo)</span>
            </h1>
            <div class="w-full h-80 rounded-xl overflow-hidden shadow-inner border border-base-300 my-4"
                 leaflet
                 [leafletOptions]="options"
                 [leafletLayers]="layers">
            </div>
            <a routerLink="/login" class="btn btn-primary mt-2">Acessar Tela de Login</a>
          </div>
        </div>
      </main>
    </div>
  `
})
export class DemoComponent implements OnInit {
  options: L.MapOptions = {
    layers: [
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; OpenStreetMap contributors'
      })
    ],
    zoom: 13,
    center: L.latLng(-7.115, -34.863)
  };

  layers: L.Layer[] = [];

  ngOnInit(): void {
    const defaultIcon = L.icon({
      iconUrl: 'leaflet/marker-icon.png',
      iconRetinaUrl: 'leaflet/marker-icon-2x.png',
      shadowUrl: 'leaflet/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      tooltipAnchor: [16, -28],
      shadowSize: [41, 41]
    });
    L.Marker.prototype.options.icon = defaultIcon;

    this.layers = [
      L.marker([-7.115, -34.863]).bindPopup('<b>A Caminho</b><br>Ponto de Embarque / Rota Universitária.')
    ];
  }
}

