/**
 *  Author: Baur
 */
 // TODO
 // Add 'disable timer' for motion, i.e. deactivate turn back on due to motion to be used for 'stealth mode' etc
 // Change minutes to seconds?
 // Add 'blackout' times for motion detection?
 // Add requirements for other lights to be off?
 // DO not turn light on if light was turned off, or double pressed, etc...
 // debounce for motion when shutting off
 // multiple OR'd motion sensors
 // multiple AND'd motion sensors
 

definition(
    name: "Motion Activated Lighting",
    namespace: "baurandr",
    author: "A. Baur",
    description: "Turn your lights on to set level when motion is detected and then off again once the motion stops for a set period of time.",
    category: "Convenience",
    parent: "baurandr:Baur Lighting",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
	section("Turn on when there's movement..."){
		input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("And off when there's been no movement for..."){
		input "minutes1", "number", title: "Minutes?"
	}
	section("Turn on/off light(s)..."){
		input "switches", "capability.switchLevel", multiple: true
	}
	section("Set dim level..."){
		input "dimLevel", "number", title: "%?"
	}    
    section("Turn on between what times? Both absolute and sun event based times must be true to turn lights on.") {
        input "fromTime", "time", title: "Start of allowed time window", required: false
        input "toTime", "time", title: "End of allowed time window", required: false
        input "onOffset", "number", title: "Start of allowed time based on Sunset offset (+ = after, - = before)", required: false
        input "offOffset", "number", title: "End of allowed time based on Sunrise offset (+ = after, - = before)", required: false

	}
    section("Turn on during what modes?") {
    	input "modesTurnOnAllowed", "mode", title: "select a mode(s)", multiple: true, required: false
    }
}

def installed() {
	initDimmers()
	subscribe(motion1, "motion", motionHandler)
}

def updated() {
	unsubscribe()
    initDimmers()
	subscribe(motion1, "motion", motionHandler)
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"

	def offSwitches = state.switchesToTurnOff
    if (evt.value == "active"){ //Motion has started
        //check time
		def timeOK = true
        if(fromTime && toTime){ 
            timeOK = timeOfDayIsBetween(fromTime, toTime, new Date(), location.timeZone)
		}
        if(onOffset && offOffset && timeOK){ 
        	def sunTimes = getSunriseAndSunset()
            def sunsetTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunTimes.sunset)
            def sunriseTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunTimes.sunrise)
            
            //calculate the offset
    		def timeAfterSunset = new Date(sunsetTime.time + (onOffset * 60 * 1000))
    		def timeAfterSunrise = new Date(sunriseTime.time + (offOffset * 60 * 1000))
            
            timeOK = timeOfDayIsBetween(timeAfterSunset, timeAfterSunrise, new Date(), location.timeZone)
		}
        
		//check mode
        def curMode = location.currentMode
        def modeOK = !modesTurnOnAllowed || modesTurnOnAllowed.contains(curMode)

        log.debug "Current Mode: ${curMode}, Turn on Mode OK: ${modeOK}"
        log.debug "Turn On time frame OK: ${timeOK}"  

        if (timeOK & modeOK) {
            checkAndSetDimmers()
        }
	} else if (evt.value == "inactive") {
		if (offSwitches) {
            if(minutes1 <= 0) {
                turnOffDimmers() 
                log.debug "Motion has stopped and desired hold time is zero, turning lights off"
            } else {
                runIn(minutes1 * 60, scheduleCheck, [overwrite: false])
            }
		}
	}
}

def scheduleCheck() {
	log.debug "schedule check"
	def motionState = motion1.currentState("motion")
    if (motionState.value == "inactive") {
        def elapsed = now() - motionState.rawDateCreated.time
    	def threshold = 1000 * 60 * minutes1 - 1000
    	if (elapsed >= threshold) {
            log.debug "Motion has stayed inactive long enough since last check ($elapsed ms):  turning lights off"
            turnOffDimmers()
    	} else {
        	log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms):  doing nothing"
        }
    } else {
    	log.debug "Motion is active, do nothing and wait for inactive"
    }
}

def checkAndSetDimmers() {
	def offSwitches = state.switchesToTurnOff
	def newOffSwitches = switches.findAll{it.currentValue("switch") == "off"}
    log.debug "Switches to be turned on by this app: ${newOffSwitches}"
	if(newOffSwitches){
    	newOffSwitches.each {it.setLevel(dimLevel)}
        //add the switches that will be turned on to the list to turn off when motion stops
        newOffSwitches.each {offSwitches << it.displayName}	
        state.switchesToTurnOff = offSwitches
	}
}

def turnOffDimmers() {
	def offSwitches = state.switchesToTurnOff
    log.debug "Switches to be turned off by this app: ${offSwitches}"
    if(offSwitches){
    	switches.each {
        	if(offSwitches.contains(it.displayName)){
            	it.setLevel(0)
			}
        }
    }
    state.switchesToTurnOff = []
}

def initDimmers() {
	state.switchesToTurnOff = []
    log.debug "Initializing" 
}