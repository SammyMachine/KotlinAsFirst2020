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

    private var array = mutableSetOf<Train>()

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

    fun addTrain(train: String, depart: Time, destination: Stop): Boolean {
        val check = array.find { it.name == train }
        return if (check != null) {
            false
        } else {
            array.add(Train(train, stopDepart(depart), destination))
            true
        }
    }

    /**
     * Удалить существующий поезд.
     *
     * Если поезда с таким именем нет, следует вернуть false и ничего не изменять в таблице
     *
     * @param train название поезда
     * @return true, если поезд успешно удалён, false, если такой поезд не существует
     */
    fun removeTrain(train: String): Boolean {
        val check = array.find { it.name == train }
        return if (check == null)
            false
        else {
            array.remove(check)
            true
        }
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
        for (element in array) {
            if (element.name == train)
                if (element.stationAvailability(element.stops, stop.name)) {
                    element.checkForTimeIfAvailable(element, stop)
                    element.timeStationChange(element, stop)
                    result = false
                    break
                } else {
                    element.checkForTimeIfNotAvailable(element, stop)
                    array.remove(element)
                    array.add(element.addIntermediateStation(element, stop))
                    result = true
                    break
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
        for (element in array) {
            if (train == element.name) {
                if (stopName == element.departStation.name || stopName == element.destinationStation.name) {
                    result = false
                    break
                } else {
                    for (a in element.stops.indices)
                        if (element.stops[a].name == stopName) {
                            array.remove(element)
                            array.add(element.removeIntermediateStation(element, element.stops[a]))
                            result = true
                            break
                        }
                }
            }
        }
        return result
    }

    /**
     * Вернуть список всех поездов, упорядоченный по времени отправления с baseStationName
     */
    fun trains(): List<Train> = array.sortedBy { it.departStation.time }

    /**
     * Вернуть список всех поездов, отправляющихся не ранее currentTime
     * и имеющих остановку (начальную, промежуточную или конечную) на станции destinationName.
     * Список должен быть упорядочен по времени прибытия на станцию destinationName
     */
    fun trains(currentTime: Time, destinationName: String): List<Train> =
        array.filter { it.departStation.time >= currentTime }
            .filter { it.stationAvailability(it.stops, destinationName) }
            .sortedBy { it.needStation(it.stops, destinationName).time }


    /**
     * Сравнение на равенство.
     * Расписания считаются одинаковыми, если содержат одинаковый набор поездов,
     * и поезда с тем же именем останавливаются на одинаковых станциях в одинаковое время.
     */
    override fun equals(other: Any?): Boolean {
        var result = false
        val list1 = mutableListOf<String>()
        val list2 = mutableListOf<String>()
        val list3 = mutableListOf<Time>()
        val list4 = mutableListOf<Time>()
        val list5 = mutableListOf<String>()
        val list6 = mutableListOf<String>()
        if (other is TrainTimeTable) {
            for (element in other.array) {
                val check = array.find { it == element }
                if (check != null) {
                    list1.add(check.name)
                    list2.add(element.name)
                }
                for (a in element.stops.indices) {
                    list3.add(element.stops[a].time)
                    list5.add(element.stops[a].name)
                }
                for (a in element.stops.indices) {
                    list4.add(element.stops[a].time)
                    list6.add(element.stops[a].name)
                }
            }
            if (list1.all { it in list2 } &&
                list3.all { it in list4 } &&
                list5.all { it in list6 } &&
                list1.size == list2.size &&
                list3.size == list4.size &&
                list5.size == list6.size)
                result = true
        }
        return result
    }

    override fun hashCode(): Int {
        var result = baseStationName.hashCode()
        result = 31 * result + array.hashCode()
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

    fun timeStationChange(train: Train, stop: Stop) {
        for (i in train.stops.indices)
            if (train.stops[i].name == stop.name) {
                train.stops[i].time = stop.time
                break
            }
    }

    fun checkForTimeIfAvailable(train: Train, stop: Stop) {
        when (stop.name) {
            train.departStation.name -> {
                for (i in 1 until train.stops.size - 1)
                    if (train.stops[i].time == stop.time || train.stops[i].time < stop.time) throw IllegalArgumentException()
            }
            train.destinationStation.name -> {
                for (i in 0 until train.stops.size - 2)
                    if (train.stops[i].time == stop.time || train.stops[i].time > stop.time) throw IllegalArgumentException()
            }
            else -> {
                for (i in train.stops.indices) {
                    if (stop.name == train.stops[i].name)
                        if (stop.time <= train.stops[i - 1].time || stop.time >= train.stops[i + 1].time) throw IllegalArgumentException()
                    break
                }
            }
        }
    }

    fun checkForTimeIfNotAvailable(train: Train, stop: Stop) {
        if (stop.time <= train.departStation.time || stop.time >= train.destinationStation.time) throw IllegalArgumentException()
    }

    fun addIntermediateStation(train: Train, stop: Stop): Train {
        val list = train.stops.toMutableList()
        if (train.stops.size == 2)
            list.add(1, stop)
        else {
            if (train.departStation.time < stop.time) {
                for (i in 1 until train.stops.size - 1) {
                    if (train.stops[i + 1].name != train.destinationStation.name) {
                        if (train.stops[i + 1].time > stop.time) {
                            list.add(i + 1, stop)
                            break
                        }
                    } else {
                        list.add(i + 1, stop)
                        break
                    }
                    list.add(i + 1, stop)
                }
            }
        }
        return Train(train.name, list)
    }

    fun removeIntermediateStation(train: Train, stop: Stop): Train {
        val list = train.stops.toMutableList()
        list.remove(stop)
        return Train(train.name, list)
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


