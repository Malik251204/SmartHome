// Room domain types.
import type { Sensor } from './sensor'
import type { Device } from './device'
import type { User } from './user'

// List-view shape — counts instead of full nested lists. backend/actual
// has no lightweight list endpoint, so roomService derives these
// client-side from the full RoomDto.
export interface Room {
  id: string
  name: string
  sensorCount: number
  userCount: number
}

export type RoomInput = { name: string }

// Full contents, for the room detail/drill-in page.
export interface RoomDetail {
  id: string
  name: string
  sensors: Sensor[]
  devices: Device[]
  users: User[]
}
