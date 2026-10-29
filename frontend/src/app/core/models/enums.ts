export enum UserRole {
  ADMIN = 'ADMINISTRADOR',
  MUNICIPALITY = 'PREFEITURA',
  BUS_DRIVER = 'MOTORISTA',
  STUDENT = 'ESTUDANTE'
}

export enum Shift {
  MORNING = 'MANHA',
  AFTERNOON = 'TARDE',
  NIGHT = 'NOITE'
}

export enum TravelStatus {
  IN_PROGRESS = 'EM_ANDAMENTO',
  COMPLETED = 'CONCLUIDA',
  CANCELLED = 'CANCELADA'
}

export enum PollDirection {
  OUTBOUND = 'Ida',
  INBOUND = 'Volta'
}

export function isOutboundTravel(direction?: string | PollDirection | null): boolean {
  if (!direction) return true;
  const d = direction.toString().trim().toUpperCase();
  return d === 'IDA' || d === 'OUTBOUND';
}

export function isInboundTravel(direction?: string | PollDirection | null): boolean {
  if (!direction) return false;
  const d = direction.toString().trim().toUpperCase();
  return d === 'VOLTA' || d === 'INBOUND';
}

export function isCompletedTravel(status?: string | TravelStatus | null): boolean {
  if (!status) return false;
  const s = status.toString().trim().toUpperCase();
  return s === 'COMPLETED' || s === 'CONCLUIDA' || s === 'CONCLUÍDA';
}


