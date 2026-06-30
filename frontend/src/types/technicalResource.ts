export interface TechnicalResource {
  id?: number
  name: string
  description?: string
  quantity?: number
  creatorId?: number
}

export interface TechnicalResourcePayload {
  name: string
  description?: string
  quantity: number
  creatorId?: number
}
