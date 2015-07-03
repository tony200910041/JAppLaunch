package myjava.io;

import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public final class IconLoader
{
	private static Map<File, Icon> ICON_32_CACHE = new HashMap<>();
	private static Map<File, Icon> ICON_16_CACHE = new HashMap<>();
	public static Icon getIcon32(File f)
	{
		try
		{
			if (ICON_32_CACHE.containsKey(f))
			{
				return ICON_32_CACHE.get(f);
			}
			/*
			 * a hack using reflection
			 * sun.awt.shell.ShellFolder is internal proprietary API
			 * may be removed in a future release
			 * may not be portable between different platform
			 */
			Class<?> c = Class.forName("sun.awt.shell.ShellFolder");
			Method m1 = c.getDeclaredMethod("getShellFolder",File.class);
			Object o1 = m1.invoke(null,f);
			Method m2 = o1.getClass().getDeclaredMethod("getIcon",boolean.class);
			m2.setAccessible(true);
			Object o2 = m2.invoke(o1,true);
			ImageIcon icon = new ImageIcon((Image)o2);
			ICON_32_CACHE.put(f, icon);
			return icon;
		}
		catch (Exception ex)
		{
			return getIcon16(f);
		}
	}
	
	public static Icon getIcon16(File f)
	{
		if (ICON_16_CACHE.containsKey(f))
		{
			return ICON_16_CACHE.get(f);
		}
		else
		{
			Icon icon = FileSystemView.getFileSystemView().getSystemIcon(f);
			ICON_16_CACHE.put(f, icon);
			return icon;
		}
	}
	
	public static void reloadCache()
	{
		//32x32
		Set<File> files1 = new HashSet<File>(ICON_32_CACHE.keySet());
		ICON_32_CACHE.clear();
		for (File file: files1)
		{
			getIcon32(file);
		}
		//16x16
		Set<File> files2 = new HashSet<File>(ICON_16_CACHE.keySet());
		ICON_16_CACHE.clear();
		for (File file: files2)
		{
			getIcon16(file);
		}
	}
}
