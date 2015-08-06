/**
 *  Keep Garage Door Closed
 *
 *  Author: Minollo
 */
preferences {
	section("Garage door") {
		input "contactSensor", "capability.contactSensor", title: "Open/close sensor?"
		input "doorSwitch", "capability.switch", title: "Door switch?"
	}
	section("Away mode") {
		input "awayMode", "mode", title: "Away mode?"
		input "maxOpenTimeWhenAway", "number", title: "Minutes?"
	}
	section("Home mode") {
		input "maxOpenTimeWhenHome", "number", title: "Minutes?"
	}
}

def installed()
{
	subscribe(contactSensor, "contact.open", contactSensorOpenHandler)
	subscribe(contactSensor, "contact.closed", contactSensorClosedHandler)
	subscribe(doorSwitch, "switch.on", doorSwitchHandler)
    updateState()
}

def updated()
{
	unsubscribe()
	subscribe(contactSensor, "contact.open", contactSensorOpenHandler)
	subscribe(contactSensor, "contact.closed", contactSensorClosedHandler)
	subscribe(doorSwitch, "switch.on", doorSwitchHandler)
    updateState()
}

def contactSensorOpenHandler(evt)
{
	log.debug "Garage just opened; start counting"
	updateState()
}

def contactSensorClosedHandler(evt)
{
   	log.debug "Garage just closed; we are fine"
	unschedule(closeGarage)
}

private updateState()
{
    unschedule(closeGarage)
	if (contactSensor.currentValue("contact") == "open") {
    	if (location.mode == awayMode) {
   	    	log.debug "Close garage in $maxOpenTimeWhenAway minutes"
	    	runIn(maxOpenTimeWhenAway * 60, closeGarage)
    	} else {
   	    	log.debug "Close garage in $maxOpenTimeWhenHome minutes"
    		runIn(maxOpenTimeWhenHome * 60, closeGarage)
        }
    }
}

def closeGarage()
{
	log.debug "Time to close the garage"
	if (contactSensor.currentValue("contact") == "open") {
    	log.debug "Push button"
    	doorSwitch.push()
        updateState()
	}
}

def doorSwitchHandler(evt)
{
	log.debug "Button pushed"
}

