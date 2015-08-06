/**
 *  Virtual Thermostat
 *
 *  Author: SmartThings
 */

// Automatically generated. Make future change here.
definition(
    name: "Manage Laundry",
    namespace: "",
    author: "minollo@minollo.com",
    description: "Manage laundry",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png")

preferences {
	section("Manage temperature... "){
		input "tempSensor", "capability.temperatureMeasurement", title: "Temperature sensor"
		input "heaterOutlet", "capability.switch", title: "Heater outlet"
		input "setpoint", "decimal", title: "Set temperature"
        input "heatInAwayMode", "bool", title: "Heat also in away mode"
	}
	section("Manage light..."){
		input "motionSensor", "capability.motionSensor", title: "Motion sensor"
		input "minutes", "number", title: "Minutes timeout"
        input "lightSwitch", "capability.switch", title: "Light switch"
	}
	section("Away mode") {
		input "awayMode", "mode", title: "Away mode"

	}
}

def installed()
{
	log.debug "Installed app"
    state.manualOn = false
	subscribe(tempSensor, "temperature", temperatureHandler)
	subscribe(motionSensor, "motion", motionHandler)
    subscribe(lightSwitch, "switch", lightSwitchHandler)
    subscribe(location, modeChangeHandler)
	subscribe(app, appTouch)
    updateState()
}

def updated()
{
	log.debug "Updated app"
    state.manualOn = false
	unsubscribe()
	subscribe(tempSensor, "temperature", temperatureHandler)
	subscribe(motionSensor, "motion", motionHandler)
    subscribe(lightSwitch, "switch", lightSwitchHandler)
    subscribe(location, modeChangeHandler)
	subscribe(app, appTouch)
    updateState()
}

def appTouch(evt)
{
	log.debug "appTouch: $evt, $settings"
	state.manualOn = true
    updateState()
}

def modeChangeHandler(evt)
{
	if (evt.value == awayMode) {
		log.debug "[Manage Laundry] Location mode is away; shut down ${if(!heatInAwayMode) "everything" else "lights (not heat)"}"
        state.manualOn = false
    	unschedule(turnOffLight)
        lightSwitch.off()
        if (!heatInAwayMode) heaterOutlet.off()
    }
	if (heatInAwayMode || location.mode != awayMode) {
		log.debug "[Manage Laundry] Location mode is no more away or should heat in away mode"
    	updateState()
    }
}

def temperatureHandler(evt)
{
	log.debug "[Manage Laundry] Teperature event"
	updateState()
}

private updateState()
{
	log.debug "[Manage Laundry] Update temperature control"
    if (location.mode == awayMode && !state.manualOn) {
		log.debug "[Manage Laundry] Location mode is away; shut down ${if(!heatInAwayMode) "everything" else "lights (not heat)"}"
    	unschedule(turnOffLight)
        lightSwitch.off()
        if (!heatInAwayMode) heaterOutlet.off()
    }
	if (state.manualOn || heatInAwayMode || location.mode != awayMode) {
        def threshold = 0.1
        def currentTemp = tempSensor.currentValue("temperature")
        log.debug "[Manage Laundry] Current temperature: $currentTemp"
        log.debug "[Manage Laundry] Target temperature: $setpoint"
        if (setpoint - currentTemp >= threshold) {
            log.debug "[Manage Laundry] Heater outlet must be on"
            heaterOutlet.on()
        } else if (currentTemp - setpoint >= threshold) {
            log.debug "[Manage Laundry] Heater outlet must be off"
            heaterOutlet.off()
        }
	}
}

def motionHandler(evt)
{
	if (location.mode == awayMode) {
		log.debug "[Manage Laundry] Location mode is away; do nothing"
    } else {
    	if (evt.value == "active") {
            log.debug "[Manage Laundry] Motion is active"
            unschedule(turnOffLight)
            if (lightSwitch.currentValue("switch") == "off") {
                log.debug "[Manage Laundry] Turn lights on"
                if (state.manualOffAt && (now() - state.manualOffAt) < 60 * 1000) {
                	log.debug "[Manage Laundry] Too close to switch off"
                } else {
                	lightSwitch.on()
                }
            }
        } else if (evt.value == "inactive") {
            log.debug "[Manage Laundry] Motion is inactive"
            unschedule(turnOffLight)
            runIn(minutes * 60, turnOffLight)
        }
	}
}

def lightSwitchHandler(evt)
{
	if (evt.isStateChange) {
        if (lightSwitch.currentValue("switch") == "off") {
            log.debug "[Manage Laundry] Light switch is off"
            if (evt.isPhysical()) {
            	state.manualOffAt = now()
            }
            unschedule(turnOffLight)
        } else {
            log.debug "[Manage Laundry] Light switch is on"
            unschedule(turnOffLight)
            runIn(minutes * 60, turnOffLight)
        }
	}
}

def turnOffLight()
{
	log.debug "[Manage Laundry] Scheduled turn off light"
	lightSwitch.off();
}
