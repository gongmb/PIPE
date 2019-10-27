package pipe.gui;

/**
 * 速率和令牌编辑表的抽象基准项目
 * Abstract Datum item for rate and token editng tables
 */
public class AbstractDatum {
    /**
     * 映射到初始基准项目
     * Mapping to an initial datum item
     * 如果Datum最初不是Petri网中的令牌，则该字段可以为null
     * This may be null if the Datum was not originally a token in the Petri net
     * <p>
     *  如果数据是修改后的数据，它将包含一个值，它直接映射到一些初始基准
     * It will contain a value if the data is a modified datum
     * and it maps directly to some initial datum
     * </p>
     */
    public AbstractDatum initial = null;

    /**
     * 基准编号
     * Datum id
     */
    public String id;

    /**
     * 建造者
     * Constructor
     * @param id of the datum 
     */
    AbstractDatum(String id) {
        this.id = id;
    }

    /**
     * Constructor
     * @param initial value
     * @param id of the datum 
     */
    AbstractDatum(AbstractDatum initial, String id) {
        this.id = id;
        this.initial = initial;
    }

    /**
     *如果ID已更改，则为true
     * @return true if the id has been changed
     */
    public final boolean hasBeenSet() {
        return !this.id.equals("");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractDatum)) {
            return false;
        }

        AbstractDatum that = (AbstractDatum) o;

        if (!id.equals(that.id)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
