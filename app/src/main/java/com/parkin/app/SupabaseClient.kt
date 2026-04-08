package com.parkin.app

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

val supabase = createSupabaseClient(
    supabaseUrl = "https://supabase.co",
    supabaseKey = ""
) {

    install(Auth)
    install(Postgrest)
}
