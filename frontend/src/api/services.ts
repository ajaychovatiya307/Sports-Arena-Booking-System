import api from './axios';
import type {
  ArenaCategory, ArenaPackage, Booking, CustomerProfile,
  DashboardSummary, Page, Rating, Session, SlotAvailability, Team
} from '../types';

export const AuthAPI = {
  login: (email: string, password: string) => api.post<Session>('/api/auth/login', { email, password }).then(r => r.data),
  register: (fullName: string, email: string, password: string) =>
    api.post<{ message: string }>('/api/auth/register', { fullName, email, password }).then(r => r.data)
};

export const ProfileAPI = {
  me: () => api.get<CustomerProfile>('/api/users/me').then(r => r.data),
  update: (dto: CustomerProfile) => api.put<CustomerProfile>('/api/users/me', dto).then(r => r.data),
  list: (params: Record<string, unknown>) =>
    api.get<Page<CustomerProfile>>('/api/users', { params }).then(r => r.data)
};

export const TeamAPI = {
  list: (params: Record<string, unknown>) => api.get<Page<Team>>('/api/teams', { params }).then(r => r.data),
  get: (id: number) => api.get<Team>(`/api/teams/${id}`).then(r => r.data),
  create: (dto: Team) => api.post<Team>('/api/teams', dto).then(r => r.data),
  update: (id: number, dto: Team) => api.put<Team>(`/api/teams/${id}`, dto).then(r => r.data),
  remove: (id: number) => api.delete(`/api/teams/${id}`).then(r => r.data)
};

export const CategoryAPI = {
  list: () => api.get<ArenaCategory[]>('/api/categories').then(r => r.data),
  create: (dto: ArenaCategory) => api.post<ArenaCategory>('/api/categories', dto).then(r => r.data),
  update: (id: number, dto: ArenaCategory) => api.put<ArenaCategory>(`/api/categories/${id}`, dto).then(r => r.data),
  remove: (id: number) => api.delete(`/api/categories/${id}`).then(r => r.data)
};

export const PackageAPI = {
  list: (params: Record<string, unknown>) => api.get<Page<ArenaPackage>>('/api/arena-packages', { params }).then(r => r.data),
  get: (id: number) => api.get<ArenaPackage>(`/api/arena-packages/${id}`).then(r => r.data),
  create: (dto: ArenaPackage) => api.post<ArenaPackage>('/api/arena-packages', dto).then(r => r.data),
  update: (id: number, dto: ArenaPackage) => api.put<ArenaPackage>(`/api/arena-packages/${id}`, dto).then(r => r.data),
  remove: (id: number) => api.delete(`/api/arena-packages/${id}`).then(r => r.data)
};

export const SlotAPI = {
  list: (packageId: number, date: string) =>
    api.get<SlotAvailability[]>('/api/slots', { params: { packageId, date } }).then(r => r.data),
  isBlocked: (packageId: number, date: string, timeSlot: string) =>
    api.get<{ blocked: boolean }>('/api/slots/is-blocked', { params: { packageId, date, timeSlot } }).then(r => r.data.blocked),
  upsert: (dto: SlotAvailability) => api.post<SlotAvailability>('/api/slots', dto).then(r => r.data),
  remove: (id: number) => api.delete(`/api/slots/${id}`).then(r => r.data)
};

export const BookingAPI = {
  search: (params: Record<string, unknown>) => api.get<Page<Booking>>('/api/bookings', { params }).then(r => r.data),
  get: (id: number) => api.get<Booking>(`/api/bookings/${id}`).then(r => r.data),
  create: (dto: Booking) => api.post<Booking>('/api/bookings', dto).then(r => r.data),
  cancel: (id: number) => api.put<Booking>(`/api/bookings/${id}/cancel`).then(r => r.data),
  updateStatus: (id: number, status: string) => api.put<Booking>(`/api/bookings/${id}/status`, { status }).then(r => r.data),
  bookedSlots: (packageId: number, date: string) =>
    api.get<{ timeSlot: string; status: string }[]>('/api/bookings/booked-slots', 
      { params: { packageId, date } }).then(r => r.data),
  dashboard: () => api.get<DashboardSummary>('/api/bookings/dashboard').then(r => r.data)
};

export const RatingAPI = {
  submit: (dto: Rating) => api.post<Rating>('/api/ratings', dto).then(r => r.data),
  mine: () => api.get<Rating[]>('/api/ratings/me').then(r => r.data),
  package: (packageId: number) => api.get<{ packageId: number; count: number; average: number }>
  (`/api/ratings/package/${packageId}`).then(r => r.data),
  packageList: (packageId: number) => api.get<Rating[]>(`/api/ratings/package/${packageId}/list`).then(r => r.data),
  all: () => api.get<Rating[]>('/api/ratings/all').then(r => r.data),
  aggregate: () => api.get<{ packageId: number; count: number; average: number }[]>('/api/ratings/aggregate').then(r => r.data)
};
