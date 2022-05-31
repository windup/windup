package testclasses.enumeration;

import org.hibernate.search.backend.configuration.impl.IndexWriterSetting;

import static org.hibernate.search.backend.configuration.impl.IndexWriterSetting.MAX_THREAD_STATES;

public class EnumConstClassNotOnClasspath {
    IndexWriterSetting writerSetting = IndexWriterSetting.MAX_THREAD_STATES;
    IndexWriterSetting writerSetting1 = MAX_THREAD_STATES;

    public EnumConstClassNotOnClasspath(IndexWriterSetting setting) {
        this.writerSetting = setting;
    }

    public static void main(String[] args) {
        System.out.println(MAX_THREAD_STATES + " test");
        EnumerationClassUsage.testEnum(IndexWriterSetting.TERM_INDEX_INTERVAL);
    }

    private static void testEnum(IndexWriterSetting mode) {
        //something
    }
}

class TestConstructor {

    public static void main(String[] args) {
        EnumConstClassNotOnClasspath test = new EnumConstClassNotOnClasspath(IndexWriterSetting.TERM_INDEX_INTERVAL);
    }


}
