package com.parkin.app

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

val supabase = createSupabaseClient(
    supabaseUrl = "https://ryinmsikczlkzgpytbum.supabase.co",
    supabaseKey = "sb_publishable_6wzx63ok5EPU5N3_8Pn6VA_wESzy1kV"
) {

    install(Auth)
    install(Postgrest)
}
