import * as React from "react"

import { fieldControlClass } from "@/components/ui/input"
import { cn } from "@/lib/utils"

function NativeSelect({ className, ...props }: React.ComponentProps<"select">) {
  return <select data-slot="native-select" className={cn(fieldControlClass, "h-11 pr-8", className)} {...props} />
}

export { NativeSelect }
