definition(
    name: "Baur Security",
    namespace: "baurandr",
    author: "Andrew Baur",
    description: "Container for all security automations",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home3-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@2x.png")


preferences {
    // The parent app preferences are pretty simple: just use the app input for the child app.
    page(name: "mainPage", title: "Baur Security Routines", install: true, uninstall: true, submitOnChange: true) {
        section {
            app(name: "contactSwitchOpenTooLong", appName: "Contact Switch Open Too Long", namespace: "baurandr", title: "Create Contact Switch Open Too Long Monitor", multiple: true)
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {

    log.debug "there are ${childApps.size()} child smartapps"
    childApps.each {child ->
        log.debug "child app: ${child.label}"
    }
}







