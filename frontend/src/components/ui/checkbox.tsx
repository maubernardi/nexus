import * as React from "react"

import { cn } from "@/lib/utils"

// Checkbox nativa: 20 px + etichetta cliccabile = area di attivazione ben oltre i 24 px richiesti.
function Checkbox({ className, ...props }: Omit<React.ComponentProps<"input">, "type">) {
  return <input type="checkbox" data-slot="checkbox" className={cn("size-5 shrink-0 accent-primary", className)} {...props} />
}

export { Checkbox }
