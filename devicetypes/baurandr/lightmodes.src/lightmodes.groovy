metadata {
	definition (name: "lightModes", namespace: "baurandr", author: "Andrew Baur") {
		capability "Sensor"

        attribute "sunState", "string", ["Dawn", "Day", "Dusk", "Night"]

		command "setSunState", ["string"]
	}
/*
	simulator {
		status "active": "motion:active"
		status "inactive": "motion:inactive"
	}
*/
	tiles(scale: 2) {
		multiAttributeTile(name:"sunState", type: "generic", width: 6, height: 4){
			tileAttribute ("device.sunState", key: "PRIMARY_CONTROL") {
				attributeState "Dawn", label:'Dawn', icon:"st.Outdoor.outdoor20", backgroundColor:"#ffffff"
				attributeState "Day", label:'Day', icon:"st.Weather.weather14", backgroundColor:"#ffffff"
				attributeState "Dusk", label:'Dusk', icon:"st.Weather.weather4", backgroundColor:"#ffffff"
				attributeState "Night", label:'Night', icon:"st.Bedroom.bedroom2", backgroundColor:"#ffffff"
                }
		}
		main "sunState"
		details "sunState"
	}
}

def parse(String description) {
    if (description != "updated"){
    	log.info "parse returned:${description}"
		def pair = description.split(":")
		createEvent(name: pair[0].trim(), value: pair[1].trim())
      }
}

def setSunState(newState) {
	//def newState = ""
	log.debug "Current SunState is: ${device.currentValue('sunState')}"
	log.debug "New SunState is: ${newState}"
    if(device.currentValue('sunState') != newState){
		sendEvent(name: "sunState", value: newState, isStateChange: true)    
    }
    else
    {
    	log.debug "No SunState change since new state = old state"
    }
    return []
}
