package nl.digitalekabeltelevisie.test;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.SortedMap;

public final class CharSetTest {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {



		final byte [] be = new byte[256];
		//		be[0]=0x45;
		//		be[1]=255-0xe9;
		//		be[2]=0x6e;
		//		be[3]=255-0xe2;
		for (int i = 32; i < 256; i++) {
			final byte b = (byte)i;

			be[i]=b;
		}



		//		System.out.println("    0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f");
		//		for(int j = 0; j<16;j++){
		//			System.out.print("  "+Integer.toHexString(j));
		//			for (int i = 0; i < 16; i++) {
		//				byte b = (byte)(i*16+j);
		//				if(i==0 || i==1 || i==8 || i==9){
		//					System.out.print("   ");
		//				}else{
		//					be[0]=b;
		//					String t = new String(be);
		//					System.out.print(" "+t+" ");
		//				}
		//			}
		//			System.out.println();
		//		}

		final SortedMap<String,Charset> charsets = Charset.availableCharsets();
		final Iterator<String> it=charsets.keySet().iterator();

		while(it.hasNext()){
			final String name=it.next();
			System.out.println("Charset name="+name);
			final Charset set = charsets.get(name);
			final String t = new String(be,32,222,set);
			System.out.println("test:="+t);

		}

		final String t = new String(be);
		System.out.println("Default Charset:");
		System.out.println(t);

		final Charset def=Charset.defaultCharset();
		System.out.println("default charset="+def.displayName()+"canonical"+def.name());


	}

}
