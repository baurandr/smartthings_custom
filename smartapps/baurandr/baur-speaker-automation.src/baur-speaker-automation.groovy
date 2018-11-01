definition(
    name: "Baur Speaker Automation",
    namespace: "baurandr",
    author: "Andrew Baur",
    description: "Container for all speaker automations",
    category: "My Apps",
    parent: "baurandr:Baur Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home3-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@2x.png")


preferences {
    // The parent app preferences are pretty simple: just use the app input for the child app.
    page(name: "mainPage", title: "Baur Speaker Routines", install: true, uninstall: true, submitOnChange: true) {
        section {
            app(name: "speakerNotifier", appName: "Speaker Notifier", namespace: "baurandr", title: "Create Speaker Notifier", multiple: true)
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