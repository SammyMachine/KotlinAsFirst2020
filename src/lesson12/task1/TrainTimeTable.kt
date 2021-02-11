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

    private var array = mutableListOf<Train>()

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
        for (i in array.indices) {
            if (train == array[i].name) {
                // Случай, когда baseStationName == stop.name
                if (baseStationName == stop.name) {
                    for (a in array[i].stops.indices) {
                        if (array[i].stops[a].name != stop.name) {
                            if (array[i].stops[a].time == stop.time || array[i].stops[a].time.compareTo(stop.time) == -1) throw IllegalArgumentException()
                            array[i].stops[0].time = stop.time
                            result = false
                        }
                    }
                    // Случай, когда destinationStationName == stop.name
                } else if (array[i].stops[array[i].stops.size - 1].name == stop.name) {
                    for (a in 0 until array[i].stops.size - 2) {
                        if (array[i].stops[a].time == stop.time || array[i].stops[array[i].stops.size - 2].time.compareTo(
                                stop.time
                            ) == 1
                        ) throw IllegalArgumentException()
                        array[i].stops[array[i].stops.size - 1].time = stop.time
                        result = false
                    }
                    // Случай, когда stop.name - промежуточная станция
                } else {
                    if (array[i].stops[0].time.compareTo(stop.time) == 1 || array[i].stops[array[i].stops.size - 1].time.compareTo(
                            stop.time
                        ) == -1
                    ) throw IllegalArgumentException()
                    if (array[i].stops.size == 2) {
                        array[i] = array[i].addIntermediateStation(array[i], stop)
                        result = true
                    } else if (array[i].stops.size == 3) {
                        if (array[i].stops[1].name.contains(stop.name)) {
                            array[i].stops[1].time = stop.time
                            result = false
                            break
                        } else {
                            array[i] = array[i].addIntermediateStation(array[i], stop)
                            result = true
                            break
                        }

                    } else {
                        for (a in 1 until array[i].stops.size - 1)
                            if (array[i].stops[a].name.contains(stop.name)) {
                                array[i].stops[a].time = stop.time
                                result = false
                                break
                            } else {
                                if (array[i].stops[a].name == stop.name) {
                                    array[i] = array[i].addIntermediateStation(array[i], stop)
                                    result = true
                                    break
                                }
                            }
                    }
                }
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
        for (i in array.indices) {
            if (train == array[i].name) {
                if (stopName == baseStationName || stopName == array[i].stops[array[i].stops.size - 1].name) {
                    result = false
                    break
                } else {
                    for (a in array[i].stops.indices)
                        if (array[i].stops[a].name == stopName) {
                            array[i] = array[i].removeIntermediateStation(array[i], stopName)
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
    fun trains(): List<Train> {
        val listOfTrains = mutableListOf<Train>()
        for (i in array.indices) {
            if (listOfTrains.isEmpty() && array[i].stops[0].name == baseStationName)
                listOfTrains.add(array[i])
            else if (listOfTrains.size == 1) {
                if (listOfTrains[0].stops[0].time.compareTo(array[i].stops[0].time) == -1)
                    listOfTrains.add(1, array[i])
                else
                    listOfTrains.add(0, array[i])
            } else {
                for (a in listOfTrains.indices) {
                    if (listOfTrains[a].stops[0].time.compareTo(array[i].stops[0].time) == -1) {
                        if (listOfTrains[a] != listOfTrains[listOfTrains.size - 1]) {
                            if (listOfTrains[a + 1].stops[0].time.compareTo(array[i].stops[0].time) == -1)
                                listOfTrains.add(a + 1, array[i])
                        } else listOfTrains.add(a + 1, array[i])
                    } else listOfTrains.add(a, array[i])
                }
            }
        }
        return listOfTrains
    }

    /**
     * Вернуть список всех поездов, отправляющихся не ранее currentTime
     * и имеющих остановку (начальную, промежуточную или конечную) на станции destinationName.
     * Список должен быть упорядочен по времени прибытия на станцию destinationName
     */
    fun trains(currentTime: Time, destinationName: String): List<Train> {
        val listOfTrains = mutableListOf<Train>()
        val listOfTrainsDestinationIndices = mutableListOf<Pair<String, Int>>()
        for (i in array.indices) {
            for (a in array[i].stops.indices) {
                if (array[i].stops[0].time.compareTo(currentTime) == 1 && array[i].stops[a].name == destinationName)
                    if (listOfTrains.isEmpty()) {
                        listOfTrains.add(array[i])
                        listOfTrainsDestinationIndices.add(Pair(array[i].name, a))
                    } else if (listOfTrains.size == 1) {
                        if (listOfTrains[0].stops[listOfTrainsDestinationIndices[0].second].time.compareTo(array[i].stops[a].time) == -1) {
                            listOfTrains.add(1, array[i])
                            listOfTrainsDestinationIndices.add(Pair(array[i].name, a))
                        } else {
                            listOfTrains.add(0, array[i])
                            listOfTrainsDestinationIndices.add(Pair(array[i].name, a))
                        }
                    } else {
                        for (n in listOfTrains.indices) {
                            if (listOfTrains[n].stops[listOfTrainsDestinationIndices[a].second].time.compareTo(array[i].stops[a].time) == -1) {
                                if (listOfTrains[n] != listOfTrains[listOfTrains.size - 1]) {
                                    if (listOfTrains[n + 1].stops[listOfTrainsDestinationIndices[a + 1].second].time.compareTo(
                                            array[i].stops[a].time
                                        ) == -1
                                    )
                                        listOfTrains.add(n + 1, array[i])
                                } else listOfTrains.add(n + 1, array[i])
                            } else listOfTrains.add(n, array[i])
                        }
                    }
            }
        }
        return listOfTrains
    }

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
            for (i in array.indices) {
                list1.add(array[i].name)
                list2.add(other.array[i].name)
                for (a in array[i].stops.indices) {
                    list3.add(array[i].stops[a].time)
                    list5.add(array[i].stops[a].name)
                }
                for (a in other.array[i].stops.indices) {
                    list4.add(other.array[i].stops[a].time)
                    list6.add(other.array[i].stops[a].name)
                }
            }
            if (list1.all { it in list2 } && list3.all { it in list4 } && list5.all { it in list6 } && list1.size == list2.size && list3.size == list4.size && list5.size == list6.size) result =
                true
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

    fun addIntermediateStation(train: Train, stop: Stop): Train {
        val list: MutableList<Stop> = train.stops.toMutableList()
        if (train.stops.size == 2)
            list.add(1, stop)
        else if (train.stops.size == 3) {
            if ((train.stops[1].time.compareTo(stop.time) == -1))
                list.add(2, stop)
            else list.add(1, stop)
        } else {
            for (i in 1 until train.stops.size - 2)
                if (train.stops[i].time.compareTo(stop.time) == -1) {
                    if (train.stops[i] != train.stops[train.stops.size - 1]) {
                        if (train.stops[i + 1].time.compareTo(stop.time) == 1) {
                            list.add(i + 1, stop)
                            break
                        }
                    } else list.add(i + 1, stop)
                } else list.add(i, stop)
        }
        return Train(train.name, list)
    }

    fun removeIntermediateStation(train: Train, stopName: String): Train {
        val list: MutableList<Stop> = train.stops.toMutableList()
        for (i in 1 until train.stops.size - 2) {
            if (train.stops[i].name == stopName)
                list.removeAt(i)
            break
        }
        return Train(train.name, list)
    }

}


