package edu.oregonstate.mist.locations

import com.fasterxml.jackson.annotation.JsonProperty
import edu.oregonstate.mist.api.Credentials
import io.dropwizard.Configuration

import javax.validation.Valid
import javax.validation.constraints.NotNull
import io.dropwizard.db.DataSourceFactory

class LocationConfiguration extends Configuration {
    @JsonProperty('authentication')
    @NotNull
    @Valid
    List<Credentials> credentialsList
//
//    @Valid
//    @NotNull
//    @JsonProperty("database")
//    DataSourceFactory database = new DataSourceFactory()

    @JsonProperty('locations')
    @NotNull
    @Valid
    Map<String, String> locationsConfiguration

//    @JsonProperty("database")
//    public DataSourceFactory getDataSourceFactory() {
//        database
//    }
//
//    @JsonProperty("database")
//    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
//        this.database = dataSourceFactory
//    }
}
