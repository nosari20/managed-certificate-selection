package com.nosari20.managedcertificateselection

class Event(val appID: String, val message: String){

    companion object {
        fun serialize(events: List<Event>): String {
            var str = ""
            for (event in events){
                str += (event.appID+","+event.message+"\n")
            }
            return str
        }

        fun deserialize(string: String): ArrayList<Event>  {
            var events = arrayListOf<Event>()

            val eventsRawList = string.split("\n")
            if (eventsRawList != null) {
                for (eventRaw in eventsRawList){
                    val eventRawSplitted = eventRaw.split(",")

                    if (eventRawSplitted.size == 2) {
                        events.add(
                            Event(
                                eventRawSplitted[0],
                                eventRawSplitted[1]
                            )
                        )
                    }
                }
            }
            return events
        }
    }
}