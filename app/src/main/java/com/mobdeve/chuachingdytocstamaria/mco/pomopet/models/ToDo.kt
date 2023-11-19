package com.mobdeve.chuachingdytocstamaria.mco.pomopet.models

class ToDo {
    var id: Int = -1
        private set

    var isDone: Boolean
        private set
    var label: String
        private set

    constructor(label: String){
        this.label = label
        this.isDone = false
    }

    constructor(){
        this.label = ""
        this.isDone = false
    }

    public fun setLabel(str: String){
        this.label = str
    }
}