@file:Suppress("UNUSED_PARAMETER")

package lesson12.task1

import java.lang.IllegalArgumentException


/**
 * Класс "расписание поездов".
 *
 * Общая сложность задания -- средняя, общая ценность в баллах -- 16.
 * Объект класса хранит расписание поездов для определённой станции отправления.
 * Для каждого поезда хранится конечная станция и список промежуточных.
 * Поддерживаемые методы:
 * добавить новый поезд, удалить поезд,
 * добавить / удалить промежуточную станцию существующему поезду,
 * поиск поездов по времени.
 *
 * В конструктор передаётся название станции отправления для данного расписания.
 */
class TrainTimeTable(private val baseStationName: String) {

    private var table = mutableMapOf<String, Train>()

    /**
     * Добавить новый поезд.
     *
     * Если поезд с таким именем уже есть, следует вернуть false и ничего не изменять в таблице
     *
     * @param train название поезда
     * @param depart время отправления с baseStationName
     * @param destination конечная станция
     * @return true, если поезд успешно добавлен, false, если такой поезд уже есть
     */
    private fun stopDepart(depart: Time): Stop = Stop(baseStationName, depart)

    fun addTrain(train: String, depart: Time, destination: Stop): Boolean =
        if (table.containsKey(train))
            false
        else {
            table[train] = Train(train, stopDepart(depart), destination)
            true
        }

    /**
     * Удалить существующий поезд.
     *
     * Если поезда с таким именем нет, следует вернуть false и ничего не изменять в таблице
     *
     * @param train название поезда
     * @return true, если поезд успешно удалён, false, если такой поезд не существует
     */
    fun removeTrain(train: String): Boolean =
        if (!table.containsKey(train))
            false
        else {
            table.remove(train)
            true
        }

    /**
     * Добавить/изменить начальную, промежуточную или конечную остановку поезду.
     *
     * Если у поезда ещё нет остановки с названием stop, добавить её и вернуть true.
     * Если stop.name совпадает с baseStationName, изменить время отправления с этой станции и вернуть false.
     * Если stop совпадает с destination данного поезда, изменить время прибытия на неё и вернуть false.
     * Если stop совпадает с одной из промежуточных остановок, изменить время прибытия на неё и вернуть false.
     *
     * Функция должна сохранять инвариант: время прибытия на любую из промежуточных станций
     * должно находиться в интервале между временем отправления с baseStation и временем прибытия в destination,
     * иначе следует бросить исключение IllegalArgumentException.
     * Также, время прибытия на любую из промежуточных станций не должно совпадать с временем прибытия на другую
     * станцию или с временем отправления с baseStation, иначе бросить то же исключение.
     *
     * @param train название поезда
     * @param stop начальная, промежуточная или конечная станция
     * @return true, если поезду была добавлена новая остановка, false, если было изменено время остановки на старой
     */
    fun addStop(train: String, stop: Stop): Boolean {
        var result = false
        if (table.containsKey(train)) {
            val ourTrain = table[train]!!
            if (ourTrain.stationAvailability(ourTrain.stops, stop.name)) {
                ourTrain.checkForTimeIfAvailable(ourTrain.stops, stop)
                ourTrain.timeStationChange(ourTrain.stops, stop)
                result = false
            } else {
                ourTrain.checkForTimeIfNotAvailable(ourTrain.stops, stop)
                table[train] = ourTrain.addIntermediateStation(ourTrain.stops, stop)
                result = true
            }
        }
        return result
    }


    /**
     * Удалить одну из промежуточных остановок.
     *
     * Если stopName совпадает с именем одной из промежуточных остановок, удалить её и вернуть true.
     * Если у поезда нет такой остановки, или stopName совпадает с начальной или конечной остановкой, вернуть false.
     *
     * @param train название поезда
     * @param stopName название промежуточной остановки
     * @return true, если удаление успешно
     */
    fun removeStop(train: String, stopName: String): Boolean {
        var result = false
        if (table.containsKey(train)) {
            val ourTrain = table[train]!!
            if (stopName == ourTrain.departStation.name || stopName == ourTrain.destinationStation.name) {
                result = false
            } else {
                for (a in ourTrain.stops.indices)
                    if (ourTrain.stops[a].name == stopName) {
                        table[train] = ourTrain.removeIntermediateStation(ourTrain.stops, ourTrain.stops[a])
                        result = true
                        break
                    }
            }
        }
        return result
    }

    /**
     * Вернуть список всех поездов, упорядоченный по времени отправления с baseStationName
     */
    fun trains(): List<Train> {
        val list = table.map { it.value }.toMutableList()
        return list.sortedBy { it.destinationStation.time }
    }

    /**
     * Вернуть список всех поездов, отправляющихся не ранее currentTime
     * и имеющих остановку (начальную, промежуточную или конечную) на станции destinationName.
     * Список должен быть упорядочен по времени прибытия на станцию destinationName
     */
    fun trains(currentTime: Time, destinationName: String): List<Train> {
        val list = table.filter { it.value.departStation.time >= currentTime }
            .filter { it.value.stationAvailability(it.value.stops, destinationName) }
            .map { it.value }.toMutableList()
        return list.sortedBy { it.needStation(it.stops, destinationName).time }
    }

    /**
     * Сравнение на равенство.
     * Расписания считаются одинаковыми, если содержат одинаковый набор поездов,
     * и поезда с тем же именем останавливаются на одинаковых станциях в одинаковое время.
     */
    override

    fun equals(other: Any?): Boolean {
        var result = false
        if (other is TrainTimeTable)
            if (other.table.keys == this.table.keys)
                for ((_, value) in this.table) {
                    if (other.table.containsValue(value))
                        result = true
                    else
                        break
                }
        return result
    }

    override fun hashCode(): Int {
        var result = baseStationName.hashCode()
        result = 31 * result + table.hashCode()
        return result
    }

}

/**
 * Время (часы, минуты)
 */
data class Time(val hour: Int, val minute: Int) : Comparable<Time> {
    /**
     * Сравнение времён на больше/меньше (согласно контракту compareTo)
     */
    override fun compareTo(other: Time): Int {
        return when {
            this.hour < other.hour || (this.hour == other.hour && this.minute < other.minute) -> -1
            this.hour > other.hour || (this.hour == other.hour && this.minute > other.minute) -> 1
            else -> 0
        }
    }
}

/**
 * Остановка (название, время прибытия)
 */
data class Stop(val name: String, var time: Time)

/**
 * Поезд (имя, список остановок, упорядоченный по времени).
 * Первой идёт начальная остановка, последней конечная.
 */
data class Train(val name: String, val stops: List<Stop>) {
    constructor(name: String, vararg stops: Stop) : this(name, stops.asList())

    val destinationStation = this.stops.last()
    val departStation = this.stops[0]

    fun timeStationChange(stops: List<Stop>, stop: Stop) {
        for (i in stops.indices)
            if (stops[i].name == stop.name) {
                stops[i].time = stop.time
                break
            }
    }

    fun checkForTimeIfAvailable(stops: List<Stop>, stop: Stop) {
        when (stop.name) {
            stops[0].name -> {
                for (i in 1 until stops.size - 1)
                    if (stops[i].time == stop.time || stops[i].time < stop.time) throw IllegalArgumentException()
            }
            stops.last().name -> {
                for (i in 0 until stops.size - 2)
                    if (stops[i].time == stop.time || stops[i].time > stop.time) throw IllegalArgumentException()
            }
            else -> {
                for (i in stops.indices) {
                    if (stop.name == stops[i].name)
                        if (stop.time <= stops[i - 1].time || stop.time >= stops[i + 1].time) throw IllegalArgumentException()
                    break
                }
            }
        }
    }

    fun checkForTimeIfNotAvailable(stops: List<Stop>, stop: Stop) {
        if (stop.time <= stops[0].time || stop.time >= stops.last().time) throw IllegalArgumentException()
    }

    fun addIntermediateStation(stops: List<Stop>, stop: Stop): Train {
        val list = stops.toMutableList()
        if (stops.size == 2)
            list.add(1, stop)
        else {
            for (i in 1 until stops.size - 1) {
                if (stops[i].time < stop.time)
                    if (stops[i + 1].name != stops.last().name) {
                        if (stops[i + 1].time > stop.time) {
                            list.add(i + 1, stop)
                            break
                        }
                    } else {
                        list.add(i + 1, stop)
                        break
                    }
                else {
                    list.add(i, stop)
                    break
                }
            }

        }
        return Train(this.name, list)
    }

    fun removeIntermediateStation(stops: List<Stop>, stop: Stop): Train {
        val list = stops.toMutableList()
        list.remove(stop)
        return Train(this.name, list)
    }

    fun stationAvailability(stops: List<Stop>, checkStationName: String): Boolean =
        stops.any { it.name == checkStationName }

    fun needStation(stops: List<Stop>, checkStationName: String): Stop {
        var station = Stop("", Time(0, 0))
        for (i in stops.indices)
            if (stops[i].name == checkStationName) {
                station = stops[i]
                break
            }
        return station
    }
}


