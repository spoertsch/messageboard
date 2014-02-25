import play.api.mvc.WithFilters
import play.api.GlobalSettings
import filter.CORSFilter

object Global extends WithFilters(CORSFilter()) with GlobalSettings {
}