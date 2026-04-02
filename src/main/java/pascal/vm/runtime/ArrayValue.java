package pascal.vm.runtime;

import pascal.error.RuntimeError;

/**
 * Значение-массив в VM. Хранит элементы с произвольными границами индексов (Pascal-style).
 * Например: array[1..10] of integer — индексы 1–10, не 0–9.
 */
public class ArrayValue {
    private final Object[] data;
    private final int startIndex;
    private final int endIndex;
    private final String elementType;

    public ArrayValue(int startIndex, int endIndex, String elementType) {
        if (endIndex < startIndex) {
            throw new RuntimeError("Неверные границы массива: [" + startIndex + ".." + endIndex + "]");
        }
        this.startIndex  = startIndex;
        this.endIndex    = endIndex;
        this.elementType = elementType;
        this.data        = new Object[endIndex - startIndex + 1];

        // Инициализация значениями по умолчанию
        Object defaultVal = switch (elementType.toLowerCase()) {
            case "integer" -> 0;
            case "boolean" -> false;
            case "string"  -> "";
            default        -> null;
        };
        java.util.Arrays.fill(data, defaultVal);
    }

    /** Получить элемент по паскалевскому индексу */
    public Object get(int index) {
        checkBounds(index);
        return data[index - startIndex];
    }

    /** Установить элемент по паскалевскому индексу */
    public void set(int index, Object value) {
        checkBounds(index);
        data[index - startIndex] = value;
    }

    private void checkBounds(int index) {
        if (index < startIndex || index > endIndex) {
            throw new RuntimeError("Выход за пределы массива: индекс " + index
                    + " вне диапазона [" + startIndex + ".." + endIndex + "]");
        }
    }

    public int getStartIndex() { return startIndex; }
    public int getEndIndex()   { return endIndex;   }
    public int length()        { return data.length; }

    @Override
    public String toString() {
        return "Array[" + startIndex + ".." + endIndex + "]" + java.util.Arrays.toString(data);
    }
}
