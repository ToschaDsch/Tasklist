package tasklist

import kotlinx.datetime.*
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.lang.reflect.ParameterizedType

data class DataTask(
    var task: MutableList<String>,
    var priority: String,
    var date: String,
    var tag: String
)

class TaskList {
    private var taskList: MutableList<DataTask> = mutableListOf()
    private val pathName = "tasklist.json"//"/home/toscha3030/IdeaProjects/Tasklist/maTask"
    private fun makeTag(date: String): String {
        val taskData = LocalDate.parse(date.split(" ")[0])
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
        val numberOfDays = currentDate.daysUntil(taskData)
        return when {
            numberOfDays == 0 -> "T"
            numberOfDays < 0 -> "O"
            else -> "I"
        }
    }

    private fun newTask(): MutableList<String> {
        println("Input a new task (enter a blank line to end):")
        val newTask: MutableList<String> = mutableListOf()
        var a = readln().trim()
        if (a == "") {
            println("The task is blank")
        }
        while (a != "") {
            if (a.isNotEmpty()) {
                newTask.add(a)
            }
            a = readln().trim()
        }
        return newTask
    }

    private fun newPriority(): String {
        println("Input the task priority (C, H, N, L):")
        val a = readln().trim().uppercase()
        val priorityArray = arrayOf("C", "H", "N", "L")
        if (a in priorityArray) return a
        return newPriority()
    }

    private fun newDate(itIsFirst: Boolean = true): String {
        if (!itIsFirst) println("The input date is invalid")
        println("Input the date (yyyy-mm-dd):")

        var date = readln()
        val date2 = date.split("-")

        date = if (date2.size == 3) {
            val month = date2[1].padStart(2, '0')
            val day = date2[2].padStart(2, '0')
            "${date2[0]}-$month-$day"
        } else "fail"

        val trueDate: LocalDate = try {
            LocalDate.parse(date)
        } catch (e: Exception) {
            newDate(itIsFirst = false).toLocalDate()
        }

        return trueDate.toString()
    }

    private fun newTime(itIsFirst: Boolean = true): String {
        if (!itIsFirst) println("The input time is invalid")
        println("Input the time (hh:mm):")

        val time = readln()

        val trueTime = try {
            val time2 = time.split(":")
            val hours = time2[0].padStart(2, '0')
            val minute = time2[1].padStart(2, '0')
            val time3 = "$hours:$minute"
            if (checkTime(time3)) time3 else newTime(itIsFirst = false)
        } catch (e: Exception) {
            newTime(itIsFirst = false)
        }

        return " $trueTime"
    }

    private fun checkTime(time: String): Boolean {
        val time2: List<Int>
        try {
            time2 = time.split(":").map { it.toInt() }
        } catch (e: Exception) {
            return false
        }
        return (time2[0] in 0..23 && time2[1] in 0..59)
    }

    private fun makeDate(): String {
        return newDate() + newTime()
    }

    fun makeInput() {
        val priority = newPriority()
        val date = makeDate()
        val tag = makeTag(date)
        val newTask = newTask()

        if (newTask.size > 0) taskList.add(
            DataTask(
                task = newTask,
                priority = priority,
                date = date,
                tag = tag
            )
        )
    }

    private fun editATask() {
        val editNumber = checkTheTaskNumber() - 1
        if (editNumber < 0) return

        val field = checkAField()
        when (field.uppercase()) {
            "PRIORITY" -> this.changePriority(editNumber)
            "DATE" -> this.changeDate(editNumber)
            "TIME" -> this.changeTime(editNumber)
            "TASK" -> this.changeATask(editNumber)
        }
        println("The task is changed")
    }

    private fun changeATask(taskNumber: Int) {
        val newTask = newTask()
        taskList[taskNumber].task = newTask
    }

    private fun changeTime(taskNumber: Int) {
        val newTime = newTime()
        val oldDays = taskList[taskNumber].date.split(" ")[0]
        val newDate = "$oldDays$newTime"
        taskList[taskNumber].date = newDate
    }

    private fun changePriority(taskNumber: Int) {
        val priority = newPriority()
        taskList[taskNumber].priority = priority
    }

    private fun changeDate(taskNumber: Int) {
        val date = newDate()
        val oldTime = taskList[taskNumber].date.split(" ")[1]
        val newDate = "$date $oldTime"
        taskList[taskNumber].date = newDate
        val newTag = makeTag(newDate)
        taskList[taskNumber].tag = newTag
    }

    private fun checkAField(isItFirst: Boolean = true): String {
        if (!isItFirst) println("invalid field")
        println("Input a field to edit (priority, date, time, task):")
        val listOfFields = arrayOf("priority", "date", "time", "task")
        val field = readln()
        return if (field in listOfFields) field else checkAField(false)
    }

    private fun checkTheTaskNumber(isItFirst: Boolean = true): Int {
        if (taskList.size == 0) {
            println("No tasks have been input")
            return -1
        }
        if (!isItFirst) println("Invalid task number")
        println("Input the task number (1-${taskList.size}):")
        val number = readln()
        val trueNumber: Int
        try {
            trueNumber = number.toInt()
        } catch (e: Exception) {
            return checkTheTaskNumber(false)
        }
        if (trueNumber !in 1..taskList.size) {
            return checkTheTaskNumber(false)
        }
        return trueNumber
    }

    private fun deleteATask() {
        val removeNumber = checkTheTaskNumber()
        if (removeNumber > -1) {
            taskList.removeAt(removeNumber - 1)
            println("The task is deleted")
        }
    }

    fun deleteIt() {
        if (taskList.size == 0) {
            println("No tasks have been input")
            return
        }
        this.printTask()
        this.deleteATask()
    }

    fun editIt() {
        if (taskList.size == 0) {
            println("No tasks have been input")
            return
        }
        this.printTask()
        this.editATask()
    }

    private fun printTitle() {
        println(
            "+----+------------+-------+---+---+--------------------------------------------+\n" +
                    "| N  |    Date    | Time  | P | D |                   Task                     |\n" +
                    "+----+------------+-------+---+---+--------------------------------------------+"
        )
    }

    private fun printInfo(index: Int, dateTime: String, priority: String, dueTag: String) {
        val index2 = if (index < 10) "$index " else "$index"
        val (date, time) = dateTime.split(" ")
        val priority2 = when (priority) {
            "C" -> "\u001B[101m \u001B[0m"
            "H" -> "\u001B[103m \u001B[0m"
            "N" -> "\u001B[102m \u001B[0m"
            else -> "\u001B[104m \u001B[0m"
        }
        val tag2 = when (dueTag) {
            "I" -> "\u001B[102m \u001B[0m"
            "T" -> "\u001B[103m \u001B[0m"
            else -> "\u001B[101m \u001B[0m"
        }
        print("| $index2 | $date | $time | $priority2 | $tag2 |")
    }

    private fun separateAString(string: String, length: Int): MutableList<String> {
        val listOfString: MutableList<String> = mutableListOf()
        var string2 = string
        while (string2.length > length) {
            listOfString.add(string2.substring(0, length))
            string2 = string2.substring(length, string2.length)
        }
        if (string2.isNotEmpty()) {
            listOfString.add(string2.padEnd(length, ' '))
        }
        return listOfString
    }

    private fun printATask(task: String, index: Int) {
        val length = 44
        val listOfString = task.split("\n").map { it.padEnd(length, ' ') }.toMutableList()
        val listOfString2: MutableList<String> = mutableListOf()
        for (string in listOfString) {
            listOfString2 += separateAString(string = task, length = 44)
        }
        for ((index2, string) in listOfString2.withIndex()) {
            if (index == 0 && index2 == 0) {
                println("$string|")
            } else {
                println("|    |            |       |   |   |$string|")
            }
        }
    }

    private fun printEndLine() {
        println("+----+------------+-------+---+---+--------------------------------------------+")
    }

    fun printTask() {
        if (taskList.size == 0) {
            println("No tasks have been input")
            return
        }
        printTitle()
        for ((index, item) in taskList.withIndex()) {
            printInfo(
                index = index + 1,
                dateTime = item.date,
                priority = item.priority,
                dueTag = item.tag
            )
            for ((index2, task) in item.task.withIndex()) {
                printATask(task = task, index = index2)
            }
            printEndLine()
        }
    }

    fun saveJSONFile() {

        val jsonFile = File(pathName)
        val moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val type: ParameterizedType = Types.newParameterizedType(List::class.java, DataTask::class.java)
        val taskListAdapter: JsonAdapter<List<DataTask>> = moshi.adapter(type)
        val myLine = taskListAdapter.toJson(taskList)
        jsonFile.writeText(myLine)
    }

    fun openJSONFile() {
        try {
            val jsonFile = File(pathName)

            val moshi: Moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val type: ParameterizedType = Types.newParameterizedType(List::class.java, DataTask::class.java)
            val taskListAdapter: JsonAdapter<List<DataTask>> = moshi.adapter(type)

            val taskObject = taskListAdapter.fromJson(jsonFile.readText())
            taskList = taskObject!!.toMutableList()

        } catch (e: Exception) {

        }
    }
}

fun main() {
    var play = true
    val taskList = TaskList()
    taskList.openJSONFile()

    while (play) {
        println("Input an action (add, print, edit, delete, end):")
        when (readln().uppercase()) {
            "END" -> {
                println("Tasklist exiting!")
                play = false
            }

            "ADD" -> taskList.makeInput()
            "EDIT" -> taskList.editIt()
            "DELETE" -> taskList.deleteIt()
            "PRINT" -> taskList.printTask()
            else -> println("The input action is invalid")
        }
    }
    taskList.saveJSONFile()
}


