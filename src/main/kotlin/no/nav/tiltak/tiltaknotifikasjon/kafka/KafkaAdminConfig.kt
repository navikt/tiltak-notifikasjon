package no.nav.tiltak.tiltaknotifikasjon.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Egen KafkaConsumer for adminverktøy som må søke opp enkeltmeldinger på en gitt offset
 * (f.eks. AdminController). Denne bruker en egen consumer group-id, adskilt fra
 * spring.kafka.consumer.group-id, slik at manuell seeking/lesing her ikke påvirker
 * commit-offsettene til de vanlige @KafkaListener-consumerne.
 */
@Configuration
@Profile("prod-gcp", "dev-gcp")
class KafkaAdminConfig {

    @Bean
    fun seekingKafkaConsumer(kafkaProperties: KafkaProperties): KafkaConsumer<String, String> {
        val props = kafkaProperties.buildConsumerProperties(null).toMutableMap()
        props[ConsumerConfig.GROUP_ID_CONFIG] = "tiltak-notifikasjon-admin-seek"
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 1
        return KafkaConsumer(props)
    }
}
