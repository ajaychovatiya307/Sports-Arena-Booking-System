export type Role = 'ADMIN' | 'CUSTOMER';

export interface Session {
  userId: string;
  fullName: string;
  email: string;
  role: Role;
}

export type SportType = 'FOOTBALL' | 'CRICKET' | 'BADMINTON' | 'TENNIS' | 'BASKETBALL';

export interface CustomerProfile {
  id?: number;
  userId: string;
  fullName: string;
  email: string;
  contactNumber?: string;
  address?: string;
}

export interface Team {
  id?: number;
  userId?: string;
  teamName: string;
  sportType: SportType;
  numberOfPlayers: number;
  coachName?: string;
  foundedYear: number;
}

export interface ArenaCategory {
  id?: number;
  categoryCode?: string;
  name: string;
  description?: string;
}

export type AvailabilityStatus = 'ACTIVE' | 'INACTIVE';

export interface ArenaPackage {
  id?: number;
  packageCode?: string;
  name: string;
  description?: string;
  pricePerHour: number;
  durationHours: number;
  categoryId: number;
  categoryName?: string;
  status?: AvailabilityStatus;
}

export interface SlotAvailability {
  id?: number;
  packageId: number;
  slotDate: string; // ISO yyyy-MM-dd
  timeSlot: string;
  blocked: boolean;
  reason?: string;
  override?: boolean;
}

export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CHECKED_IN' | 'COMPLETED' | 'CANCELLED';

export interface Booking {
  id?: number;
  referenceNumber?: string;
  userId?: string;
  customerName?: string;
  teamId: number;
  teamName?: string;
  packageId: number;
  packageName?: string;
  bookingDate: string; // yyyy-MM-dd
  timeSlot: string;
  specialRequest?: string;
  status?: BookingStatus;
  totalAmount?: number;
  createdAt?: string;
}

export interface Rating {
  id?: number;
  bookingId: number;
  referenceNumber?: string;
  userId?: string;
  packageId?: number;
  stars: number;
  review?: string;
}

export interface DashboardSummary {
  totalCustomers?: number;
  totalCategories?: number;
  totalPackages?: number;
  totalTeams?: number;
  totalBookings: number;
  activeBookings?: number;
  completedBookings?: number;
  bookingsByStatus: Record<string, number>;
  recentBookings?: Booking[];
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const TIME_SLOTS = [
  '06:00-07:00',
  '07:00-08:00',
  '08:00-09:00',
  '17:00-18:00',
  '18:00-19:00',
  '19:00-20:00',
  '20:00-21:00'
];

export const SPORT_TYPES: SportType[] = ['FOOTBALL', 'CRICKET', 'BADMINTON', 'TENNIS', 'BASKETBALL'];
