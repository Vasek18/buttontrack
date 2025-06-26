export interface Button {
  id: number;
  userId: number;
  title: string;
  color: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateButtonRequest {
  userId: number;
  title: string;
  color: string;
}

export interface UpdateButtonRequest {
  title: string;
  color: string;
}

export interface ButtonPressData {
  date: string; // YYYY-MM-DD format
  hour: number; // 0-23
  pressedAt: string; // ISO instant
}

export interface ButtonPressStatsResponse {
  buttonId: number;
  buttonTitle: string;
  buttonColor: string;
  presses: ButtonPressData[];
}

export interface StatsResponse {
  buttonStats: ButtonPressStatsResponse[];
}