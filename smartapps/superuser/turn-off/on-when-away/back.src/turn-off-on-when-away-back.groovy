/**
 *  Turn off/on when away/back
 *
 *  Author: minollo@minollo.com
 *  Date: 2014-03-01
 */
preferences {
	section("When mode is set to...") {
		input "controlMode", "mode", title: "Mode:"
	}
	section("...turn these off...") {
		input "offWhenMode", "capability.switch", title: "Devices", multiple: true, required: false
	}
	section("...and these on...") {
		input "onWhenMode", "capability.switch", title: "Devices", multiple: true, required: false
	}
	section("And when mode is different, turn these off...") {
		input "offWhenNotMode", "capability.switch", title: "Devices", multiple: true, required: false
	}
	section("...and these on...") {
		input "onWhenNotMode", "capability.switch", title: "Devices", multiple: true, required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(location, changedLocationMode)
	runIn(15, initialize)
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe(location, changedLocationMode)
	runIn(15, initialize)
}

def initialize() {
	handleMode(location.mode)
}

def changedLocationMode(evt) {
	log.debug "changedLocationMode: $evt"
	handleMode(evt.value)
}

private handleMode(mode) {
	log.debug "handleMode: $mode"
    if (mode == controlMode) {
    	offWhenMode?.off()
        onWhenMode?.on()
    } else {
    	offWhenNotMode?.off()
        onWhenNotMode?.on()
    }
}

