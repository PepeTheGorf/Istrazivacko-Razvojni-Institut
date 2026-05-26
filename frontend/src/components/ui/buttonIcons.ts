import addIcon from './icons/add.svg'
import deleteIcon from './icons/delete-trashcan.svg'
import editIcon from './icons/edit.svg'

export const BUTTON_ICONS = {
  add: addIcon,
  edit: editIcon,
  delete: deleteIcon,
} as const

export type ButtonIconName = keyof typeof BUTTON_ICONS

export const BUTTON_ICON_SIZE = 20
