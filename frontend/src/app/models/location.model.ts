export type LocationCategory =
  | 'HISTORICAL'
  | 'NATURE'
  | 'BEACH'
  | 'CITY_LIFE'
  | 'FOOD_DINING'
  | 'ADVENTURE'
  | 'CULTURE'
  | 'OTHER';

export interface CategoryMeta {
  label: string;
  icon: string;
  color: string;
  bgColor: string;
}

export const CATEGORY_METADATA: Record<LocationCategory, CategoryMeta> = {
  HISTORICAL: { label: 'Historical', icon: 'bank', color: '#722ed1', bgColor: '#f9f0ff' },
  NATURE: { label: 'Nature & Parks', icon: 'environment', color: '#52c41a', bgColor: '#f6ffed' },
  BEACH: { label: 'Beach & Coastal', icon: 'compass', color: '#13c2c2', bgColor: '#e6fffb' },
  CITY_LIFE: { label: 'City Life', icon: 'apartment', color: '#1677ff', bgColor: '#e6f4ff' },
  FOOD_DINING: { label: 'Food & Dining', icon: 'coffee', color: '#fa8c16', bgColor: '#fff7e6' },
  ADVENTURE: { label: 'Adventure', icon: 'thunderbolt', color: '#eb2f96', bgColor: '#fff0f6' },
  CULTURE: { label: 'Art & Culture', icon: 'camera', color: '#2f54eb', bgColor: '#f0f5ff' },
  OTHER: { label: 'Other', icon: 'global', color: '#8c8c8c', bgColor: '#fafafa' }
};

export interface LocationItem {
  id: number;
  title: string;
  city: string;
  country: string;
  latitude?: number | null;
  longitude?: number | null;
  description?: string | null;
  category: LocationCategory;
  visitedAt?: string | null;
  createdAt: string;
  updatedAt?: string | null;
}

export interface CreateLocationRequest {
  title: string;
  city: string;
  country: string;
  latitude?: number | null;
  longitude?: number | null;
  description?: string | null;
  category: LocationCategory;
  visitedAt?: string | null;
}

export interface UpdateLocationRequest {
  title: string;
  city: string;
  country: string;
  latitude?: number | null;
  longitude?: number | null;
  description?: string | null;
  category: LocationCategory;
  visitedAt?: string | null;
}

export interface LocationStats {
  totalLocations: number;
  totalCountries: number;
}
