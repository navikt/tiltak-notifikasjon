package no.nav.tiltak.tiltaknotifikasjon.utils

enum class Cluster {
    DEV_GCP, PROD_GCP, LOKAL;

    companion object {
        val current: Cluster by lazy {
            when (val c = System.getenv("NAIS_CLUSTER_NAME")) {
                "dev-gcp" -> DEV_GCP
                "prod-gcp" -> PROD_GCP
                else -> LOKAL
            }
        }
    }
}
