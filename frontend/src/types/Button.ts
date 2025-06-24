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