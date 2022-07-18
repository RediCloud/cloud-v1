import lombok.Data;

import java.io.Serializable;

@Data
public class TestObject implements Serializable {

    private final long time = System.currentTimeMillis();
    private final String line;

}
