package edu.oregonstate.mist.locations

import com.fasterxml.jackson.annotation.JsonProperty
import edu.oregonstate.mist.api.Credentials
import edu.oregonstate.mist.locations.core.Calendar
import io.dropwizard.Configuration
import org.hibernate.validator.constraints.NotEmpty

import javax.validation.Valid
import javax.validation.constraints.NotNull
import io.dropwizard.db.DataSourceFactory

class LocationConfiguration extends Configuration {
    @JsonProperty('authentication')
    @NotNull
    @Valid
    List<Credentials> credentialsList

    @JsonProperty("database")
    DataSourceFactory database = new DataSourceFactory()

    @JsonProperty('locations')
    @NotNull
    @Valid
    Map<String, String> locationsConfiguration

    @NotEmpty
    @JsonProperty
    List<Calendar> calendars

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
