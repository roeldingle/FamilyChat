package io.roeldingle.familychat.model

/*
   * model class to contain chat messages
   * */
class  ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val timestamp: Long){
    constructor(): this ("","","","",-1)
}