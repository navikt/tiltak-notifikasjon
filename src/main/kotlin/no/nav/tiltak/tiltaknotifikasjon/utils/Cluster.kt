package no.nav.tiltak.tiltaknotifikasjon.utils

enum class Cluster(val verdi: String) {
    DEV_GCP("dev-gcp"), PROD_GCP("prod-gcp"), LOKAL("lokal");

    companion object {
        val current: Cluster by lazy {
            when (System.getenv("NAIS_CLUSTER_NAME")) {
                "dev-gcp" -> DEV_GCP
                "prod-gcp" -> PROD_GCP
                else -> LOKAL
            }
        }
    }
}
