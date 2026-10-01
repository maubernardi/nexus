import * as React from "react"

import { cn } from "@/lib/utils"

// Controlli di form NATIVI (più affidabili su mobile e con gli screen reader). text-base = 16 px: niente zoom su iOS.
export const fieldControlClass =
  "w-full rounded-md border border-input bg-background px-3 text-base text-foreground placeholder:text-muted-foreground disabled:opacity-60 aria-[invalid=true]:border-2 aria-[invalid=true]:border-destructive"

function Input({ className, type = "text", ...props }: React.ComponentProps<"input">) {
  return <input type={type} data-slot="input" className={cn(fieldControlClass, "h-11", className)} {...props} />
}

export { Input }
