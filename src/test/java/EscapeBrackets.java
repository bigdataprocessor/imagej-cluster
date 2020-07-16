public class EscapeBrackets
{
	public static void main( String[] args )
	{
		final String s = "fasfsd(sdfsf)sfsf";
		final String replace = s.replace( "(", "\\(" ).replace( ")", "\\)" );
		System.out.println(replace);
	}
}
