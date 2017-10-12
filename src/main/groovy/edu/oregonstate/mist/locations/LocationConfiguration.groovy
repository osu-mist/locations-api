package edu.oregonstate.mist.locations

import com.fasterxml.jackson.annotation.JsonProperty
import edu.oregonstate.mist.api.Configuration
import edu.oregonstate.mist.api.Credentials
import edu.oregonstate.mist.locations.core.Calendar
import io.dropwizard.client.HttpClientConfiguration
import org.hibernate.validator.constraints.NotEmpty

import javax.validation.Valid
import javax.validation.constraints.NotNull
import io.dropwizard.db.DataSourceFactory

class LocationConfiguration extends Configuration {
    @JsonProperty("database")
    DataSourceFactory database = new DataSourceFactory()

    @JsonProperty('locations')
    @NotNull
    @Valid
    Map<String, String> locationsConfiguration

    @Valid
    @NotNull
    HttpClientConfiguration httpClientConfiguration
}
