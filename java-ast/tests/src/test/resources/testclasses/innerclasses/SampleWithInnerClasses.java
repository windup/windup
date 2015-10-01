package testclasses.innerclasses;

public class SampleWithInnerClasses
{
    public class SimpleNested
    {
        int a;
    }

    public SimpleNested returnSimpleNested()
    {
        return new SimpleNested();
    }

    public Object useDefinedInside()
    {
        final class DefinedInside
        {
            public String toString()
            {
                return "defined inside";
            }
        }

        return new DefinedInside();
    }

    public Object useInnerAnonymous()
    {
        return new SimpleNested()
        {
            public String toString()
            {
                return "Inner Anonymous";
            }
        };
    }

    public Object useInnerAnonymousUnbound()
    {
        return new ThisClassDoesNotExist()
        {
            public String toString()
            {
                return "Inner anonymous Class that does not exist";
            }
        };
    }
}