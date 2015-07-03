import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import java.util.*;
import java.io.*;
import sun.awt.shell.ShellFolder;
import MyJava.GUI.*;

public class JAppLaunch extends JFrame
{
	private static final float VERSION_NO = (float)1.0;
	private static final Font f13 = new Font("Microsoft Jhenghei", Font.PLAIN, 13);
	private static final Border bord = new LineBorder(Color.BLACK, 1);
	
	private static final int WIDTH = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	private static final int HEIGHT = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	
	private static final File SettingsFile = new File(getJARPath() + "\\JAPPLAUNCHPREFS.PROPERTIES");
	private static final Properties prop = new Properties();
	
	final JTabbedPane tPane = new JTabbedPane();
	final JPanel gridPanel = new JPanel();
	final JMenuBar menuBar = new JMenuBar();
	final JTextField searchBar = new JTextField();
	
	final MyTab tab1 = new MyTab("Programs");
	final MyTab tab2 = new MyTab("Files");
	final MyTab tab3 = new MyTab("Others");
	
	static JAppLaunch w;
	static MyFileChooser chooser;
	static boolean confirmDrag = true;
	static boolean Icon = true;
	
	static final boolean useTray = SystemTray.isSupported();
	static final PopupMenu popup = new PopupMenu();
	static TrayIcon trayIcon;
	
	static int i, j, k, l;
	static String TMP1, TMP2, TMP3;
	
	public static void main(final String[] args)
	{
		final SplashScreen splash = SplashScreen.getSplashScreen();
		splash.createGraphics();
		try
		{
			switch (getConfig("LAF"))
			{
				case "Windows":
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				break;
				
				case "Nimbus":
				UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
				break;
			}
		}
		catch (Throwable ex)
		{
		}
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				w = new JAppLaunch("JAppLaunch " + VERSION_NO);				
				(new SwingWorker<Void, Void>()
				{
					@Override
					public Void doInBackground()
					{
						w.restoreChooser();
						return null;
					}
				}).execute();
				
				(new SwingWorker<Void, Void>()
				{
					@Override
					public Void doInBackground()
					{
						w.restoreTextField();
						return null;
					}
				}).execute();
				
				(new SwingWorker<Void, Void>()
				{
					@Override
					public Void doInBackground()
					{
						w.restoreMenu();
						return null;
					}
				}).execute();
				
				(new SwingWorker<Void, Void>()
				{
					@Override
					public Void doInBackground()
					{
						w.restoreDrag();
						return null;
					}
				}).execute();
				
				(new SwingWorker<Void, Void>()
				{
					@Override
					public Void doInBackground()
					{
						if (useTray)
						{
							try
							{
								addIconToTray();
							}
							catch (Exception ex)
							{
							}
						}
						else
						{
							JOptionPane.showMessageDialog(w, "System tray is not supported!", "Error", JOptionPane.ERROR_MESSAGE);
						}
						return null;
					}
				}).execute();
				splash.close();
				if (args.length == 1)
				{
					if (!args[0].replace("/", "-").equals("-startup"))
					{
						w.setVisible(true);
					}
				}
				else
				{
					w.setVisible(true);
				}
			}
		});
	}
	
	public static void addIconToTray() throws AWTException
	{		
		if (useTray)
		{
			MyAWTMenuItem item1 = new MyAWTMenuItem("Show/Hide", 1);
			MyAWTMenuItem item2 = new MyAWTMenuItem("Close", 2);
			popup.add(item1);
			popup.add(item2);
			trayIcon = new TrayIcon((new ImageIcon(w.getClass().getResource("MyJava/SRC/APPICON.PNG"))).getImage(), "JAppLaunch " + VERSION_NO, popup);
			trayIcon.setImageAutoSize(true);
			trayIcon.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseReleased(MouseEvent ev)
				{
					if (!ev.isPopupTrigger())
					{
						w.setVisible(!w.isVisible());
					}
				}
			});
			SystemTray.getSystemTray().add(trayIcon);
		}
	}
	
	static class MyAWTMenuItem extends MenuItem implements ActionListener
	{
		private int x;
		public MyAWTMenuItem(String str, int x)
		{
			super(str);
			this.setFont(JAppLaunch.f13);
			this.addActionListener(this);
			this.x = x;
		}
		
		@Override
		public void actionPerformed(ActionEvent ev)
		{
			switch (x)
			{
				//show/hide
				case 1:
				w.setVisible(!w.isVisible());
				break;
				
				//close
				case 2:
				if (useTray)
				{
					SystemTray.getSystemTray().remove(trayIcon);
				}
				w.saveSizeAndLocation();
				System.exit(0);
				break;
			}
		}
	}
	
	public JAppLaunch(String str)
	{
		super(str);		
		this.initialize();		
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setSizeAndLocation();
		try
		{
			this.setIconImage((new ImageIcon(getClass().getResource("MyJava/SRC/APPICON.PNG"))).getImage());
		}
		catch (Exception ex)
		{
		}
		this.setLayout(new BorderLayout());
		this.add(tPane, BorderLayout.CENTER);
		
		menuBar.setBackground(Color.WHITE);
		this.setJMenuBar(menuBar);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(searchBar, BorderLayout.PAGE_START);
		
		gridPanel.setLayout(new GridLayout(2,4,5,5));
		topPanel.add(gridPanel, BorderLayout.CENTER);
		this.add(topPanel, BorderLayout.PAGE_START);
		
		restoreGrid();
		load();
	}
	
	public void restoreChooser()
	{
		tPane.setFont(f13);
		JComponent.setDefaultLocale(java.util.Locale.ENGLISH);
		chooser = new MyFileChooser("Please choose a file:");
	}
	
	public void restoreTextField()
	{
		searchBar.setFont(f13);		
		searchBar.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent ev)
			{
				TMP1 = searchBar.getText();
				JList myList = ((MyTab)(tPane.getSelectedComponent())).list;
				DefaultListModel model = (DefaultListModel)(myList.getModel());
				j = model.size();
				for (i=0; i<j; i++)
				{
					if ((TMP2 = getConfig(model.getElementAt(i) + ".name")) == null)
					{
						TMP2 = (new File(getConfig(model.getElementAt(i).toString()))).getName();
					}
					if (TMP2.contains(TMP1))
					{
						myList.setSelectedIndex(i);
						myList.ensureIndexIsVisible(i);
						return;
					}
				}
			}
		});
	}
	
	public void restoreGrid()
	{
		MyGrid g1 = new MyGrid(1);
		MyGrid g2 = new MyGrid(2);
		MyGrid g3 = new MyGrid(3);
		MyGrid g4 = new MyGrid(4);
		MyGrid g5 = new MyGrid(5);
		MyGrid g6 = new MyGrid(6);
		MyGrid g7 = new MyGrid(7);
		MyGrid g8 = new MyGrid(8);
	}
	
	public void restoreMenu()
	{
		MyMenu menu1 = new MyMenu("JAppLaunch");
		menu1.add(new MyMenuItem("Add new item to Programs", 1));
		menu1.add(new MyMenuItem("Add new item to Files", 2));
		menu1.add(new MyMenuItem("Add new item to Others", 3));
		menu1.add(new JSeparator());
		menu1.add(new MyMenuItem("Enable/disable always on top", 7));
		menu1.add(new MyMenuItem("Set Look and Feel", 8));
		menu1.add(new MyMenuItem("Other settings", 9));
		menu1.add(new JSeparator());
		menu1.add(new MyMenuItem("Close to tray", 4));
		menu1.add(new MyMenuItem("Close", 5));
		
		MyMenu menu2 = new MyMenu("About");
		menu2.add(new MyMenuItem("About JAppLaunch", 6));
	}
	
	public void restoreDrag()
	{
		tab1.list.setDropTarget(new MyDropTarget(1));
		tab2.list.setDropTarget(new MyDropTarget(2));
		tab3.list.setDropTarget(new MyDropTarget(3));
	}
	
	class MyDropTarget extends DropTarget
	{
		private int x;
		public MyDropTarget(int x)
		{
			this.x = x;
		}
		
		@Override
		public synchronized void drop(DropTargetDropEvent dtde)
		{
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			java.util.List list = null;
			try
			{
				list = (java.util.List)(dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
			}
			catch (Throwable ex)
			{
			}
			for (Object obj: list)
			{
				if (obj instanceof File)
				{
					File file = (File)obj;
					switch (x)
					{
						case 1:
						if (confirmDrag)
						{
							i = JOptionPane.showConfirmDialog(w, "Add " + file.getPath() + " to Programs?", "Confirm", JOptionPane.YES_NO_OPTION);
						}
						if ((i == JOptionPane.YES_OPTION)||(!confirmDrag))
						{
							k = getNextUsable("Programs");
							writeConfig("Programs." + k, file.getPath());
							tab1.list.lm.addElement("Programs." + k);
						}
						break;
						
						case 2:
						if (confirmDrag)
						{
							i = JOptionPane.showConfirmDialog(w, "Add " + file.getPath() + " to Files?", "Confirm", JOptionPane.YES_NO_OPTION);
						}
						if ((i == JOptionPane.YES_OPTION)||(!confirmDrag))
						{
							k = getNextUsable("Files");
							writeConfig("Files." + k, file.getPath());
							tab2.list.lm.addElement("Files." + k);
						}
						break;
						
						case 3:
						if (confirmDrag)
						{
							i = JOptionPane.showConfirmDialog(w, "Add " + file.getPath() + " to Others?", "Confirm", JOptionPane.YES_NO_OPTION);
						}
						if ((i == JOptionPane.YES_OPTION)||(!confirmDrag))
						{
							k = getNextUsable("Others");
							writeConfig("Others." + k, file.getPath());
							tab3.list.lm.addElement("Others." + k);
						}
						break;
					}
				}
			}
		}
	}
	
	public void load()
	{
		File path = getJARPath();
		while (path.getParentFile() != null)
		{
			path = path.getParentFile();
		}
		TMP3 = getConfig("LastDrive");
		//path = root
		java.util.List list = Collections.list(prop.propertyNames());
		Collections.sort(list, new Comparator<String>()
		{
			@Override
			public int compare(String str1, String str2)
			{
				if ((TMP1 = getConfig(str1 + ".name")) == null)
				{
					TMP1 = (new File(getConfig(str1))).getName();
				}
				if ((TMP2 = getConfig(str2 + ".name")) == null)
				{
					TMP2 = (new File(getConfig(str2))).getName();
				}
				return TMP1.compareTo(TMP2);
			}
		});
		for (Object item: list)
		{
			TMP1 = (String)item;
			if (!TMP1.endsWith("name"))
			{
				try
				{
					if (getConfig("isPortable").equals("true"))
					{
						if (!path.getPath().equals(TMP3))
						{
							writeConfig(TMP1, getConfig(TMP1).replace(TMP3, path.getPath()));
						}
					}
				}
				catch (Throwable ex)
				{
				}
				if (TMP1.startsWith("Programs"))
				{
					tab1.list.lm.addElement(TMP1);
				}
				else if (TMP1.startsWith("Files"))
				{
					tab2.list.lm.addElement(TMP1);
				}
				else if (TMP1.startsWith("Others"))
				{
					tab3.list.lm.addElement(TMP1);
				}
			}
		}
		writeConfig("LastDrive", path.getPath());
	}
	
	class MyTab extends JPanel
	{
		final MyList list = new MyList();
		public MyTab(String tabName)
		{
			this.setLayout(new BorderLayout());
			this.setBackground(Color.WHITE);
			tPane.addTab(tabName, null, this, null);
			this.add(new JScrollPane(list), BorderLayout.CENTER);
		}
		
		class MyList extends JList implements MouseListener
		{
			final DefaultListModel lm = new DefaultListModel();
			private final JPopupMenu popupMenu = new JPopupMenu();
			final MyPrivateMenuItem item = new MyPrivateMenuItem("Delete", 1);
			final MyPrivateMenuItem item2 = new MyPrivateMenuItem("Set preferred name", 2);
			public MyList()
			{
				super();
				this.setModel(lm);
				this.addMouseListener(this);
				this.setDragEnabled(true);
				this.setCellRenderer(new DefaultListCellRenderer()
				{
					@Override
					public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
					{
						JLabel c = (JLabel)(super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus));
						c.setFont(f13);
						File file = new File(getConfig((String)value));
						try
						{
							if (Icon)
							{
								c.setIcon(getIcon16(file));
							}
						}
						catch (Throwable ex)
						{
						}
						try
						{
							if ((TMP3 = getConfig(value + ".name")) != null)
							{
								c.setText(TMP3);
							}
							else throw new Exception();
						}
						catch (Throwable ex)
						{
							c.setText(file.getName());
						}
						if (isSelected)
						{
							c.setBackground(new Color(255,254,212));
							c.setForeground(Color.BLACK);
						}
						else
						{
							c.setBackground(Color.WHITE);
						}
						return c;
					}
				});
			}
			
			class MyPrivateMenuItem extends JMenuItem implements MouseListener
			{
				private int x;
				public MyPrivateMenuItem(String str, int x)
				{
					super(str);
					popupMenu.add(this);
					this.setFont(f13);
					this.setBackground(Color.WHITE);
					this.addMouseListener(this);
					this.x = x;
				}
				
				@Override
				public void mouseReleased(MouseEvent ev)
				{
					switch (x)
					{
						case 1:
						int array[] = getSelectedIndices();
						i = 0;
						if (array != null)
						{
							for (int x: array)
							{
								if (x >= 0)
								{
									TMP1 = (String)(lm.getElementAt(x-i));
									lm.removeElementAt(x-i);
									removeConfig(TMP1);
									removeConfig(TMP1 + ".name");
								}
								i++;
							}
						}
						break;
						
						case 2:
						TMP1 = JOptionPane.showInputDialog(w, "Please enter the preferred name:", "Set preferred name", JOptionPane.QUESTION_MESSAGE);
						if (TMP1 != null)
						{
							i = getSelectedIndex();
							if (!TMP1.isEmpty())
							{								
								if (i >= 0)
								{
									writeConfig(lm.getElementAt(i) + ".name", TMP1);
								}
							}
							else
							{
								removeConfig(lm.getElementAt(i) + ".name");
							}
						}
						break;
					}
				}
				
				@Override
				public void mouseEntered(MouseEvent ev)
				{
				}
				
				@Override
				public void mouseExited(MouseEvent ev)
				{
				}
				
				@Override
				public void mousePressed(MouseEvent ev)
				{
				}
				
				@Override
				public void mouseClicked(MouseEvent ev)
				{
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent ev)
			{
				if (ev.getSource() instanceof JList)
				{
					if (ev.isPopupTrigger())
					{
						popupMenu.show((Component)(ev.getSource()), ev.getX(), ev.getY());
					}
				}
			}
			
			@Override
			public void mousePressed(MouseEvent ev)
			{
			}
			
			@Override
			public void mouseClicked(MouseEvent ev)
			{
				if (ev.getSource() instanceof JList)
				{
					if ((ev.getClickCount() == 2)&&(!ev.isPopupTrigger()))
					{
						MyList list = (MyList)(ev.getSource());
						i = list.getSelectedIndex();
						if (i >= 0)
						{
							File f = new File(getConfig((String)(this.lm.getElementAt(i))));
							try
							{
								openFile(f);
								(new Thread()
								{
									@Override
									public void run()
									{
										JWindow window = new JWindow();
										JLabel label = new JLabel("   Opening...   ");
										label.setFont(f13);
										window.getContentPane().setBackground(new Color(252,255,220));
										window.getRootPane().setBorder(bord);
										window.setLayout(new FlowLayout());
										window.add(label, BorderLayout.CENTER);
										window.pack();
										window.setLocationRelativeTo(w);
										window.setAlwaysOnTop(true);
										window.setVisible(true);
										try
										{
											this.sleep(2000);
										}
										catch (Throwable ex)
										{
										}
										window.dispose();
										if (getConfig("isCloseAfterLaunch").equals("true"))
										{
											w.setVisible(false);
										}
									}
								}).start();
							}
							catch (Throwable ex)
							{
								JOptionPane.showMessageDialog(w, "Cannot open " + f.getPath() + " !\nPlease make sure the path is correct and close portable mode if necessary.", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent ev)
			{
			}
			
			@Override
			public void mouseExited(MouseEvent ev)
			{
			}
		}
	}
	
	class MyMenu extends JMenu
	{
		public MyMenu(String str)
		{
			super(str);
			this.setBackground(Color.WHITE);
			this.setForeground(Color.BLACK);
			this.setFont(f13);
			menuBar.add(new JLabel(" "));
			menuBar.add(this);
		}
	}
	
	class MyMenuItem extends JMenuItem implements MouseListener
	{
		private int x;
		public MyMenuItem(String str, int x)
		{
			super(str);
			this.setBackground(Color.WHITE);
			this.setForeground(Color.BLACK);
			this.setFont(f13);
			this.addMouseListener(this);
			this.x = x;
		}
		
		@Override
		public void mouseReleased(MouseEvent ev)
		{
			if (this.x <= 3)
			{
				j = chooser.showOpenDialog(w);
				if (j == JFileChooser.APPROVE_OPTION)
				{
					File file = chooser.getSelectedFile();
					switch (x)
					{
						case 1:
						k = getNextUsable("Programs");
						writeConfig("Programs." + k, file.getPath());
						tab1.list.lm.addElement("Programs." + k);
						break;
						
						case 2:
						k = getNextUsable("Files");
						writeConfig("Files." + k, file.getPath());
						tab2.list.lm.addElement("Programs." + k);
						break;
						
						case 3:
						k = getNextUsable("Others");
						writeConfig("Others." + k, file.getPath());
						tab3.list.lm.addElement("Programs." + k);
						break;
					}
				}
			}
			else
			{
				switch (x)
				{
					case 4: //close to tray
					w.setVisible(false);
					break;
					
					case 5: //close
					saveSizeAndLocation();
					System.exit(0);
					break;
					
					case 6: //about
					JOptionPane.showMessageDialog(w, "JAppLaunch " + VERSION_NO + " -- a file launcher written in Java.\nBy tony200910041, http://tony200910041.wordpress.com\nDistributed under MPL 2.0.", "About JAppLaunch", JOptionPane.INFORMATION_MESSAGE);
					break;
					
					case 7: //always on top
					if (w.isAlwaysOnTop())
					{
						w.setAlwaysOnTop(false);
						writeConfig("OnTop", "false");
					}
					else
					{
						w.setAlwaysOnTop(true);
						writeConfig("OnTop", "true");
					}
					break;
					
					case 8: //look and feel
					JDialog LAFOption = new JDialog(w);
					LAFOption.setModal(true);
					LAFOption.setTitle("Look and Feel option");
					LAFOption.getContentPane().setBackground(Color.WHITE);
					boolean DefaultL = false;
					boolean WindowsL = false;
					boolean Nimbus = false;
					try
					{
						TMP1 = getConfig("LAF");
						if (TMP1 == null) throw new Exception();
					}
					catch (Exception ex)
					{
						TMP1 = "Default";
					}
					finally
					{
						switch (TMP1)
						{
							case "Default":
							DefaultL = true;
							break;
							
							case "Windows":
							WindowsL = true;
							break;
							
							case "Nimbus":
							Nimbus = true;
							break;
						}					
					}
					final MyRadioButton isDefaultL = new MyRadioButton("Use default Look and Feel", DefaultL, 1);
					final MyRadioButton isWindowsL = new MyRadioButton("Use Windows Look and Feel", WindowsL, 2);
					final MyRadioButton isNimbus = new MyRadioButton("Use Nimbus Look and Feel", Nimbus, 3);
					ActionListener listener3 = new ActionListener()
					{
						public void actionPerformed(ActionEvent ev)
						{
							switch (((MyRadioButton)(ev.getSource())).getIndex())
							{
								case 1:
								isDefaultL.setSelected(true);
								isWindowsL.setSelected(false);
								isNimbus.setSelected(false);
								TMP1 = "Default";
								break;
								
								case 2:
								isDefaultL.setSelected(false);
								isWindowsL.setSelected(true);
								isNimbus.setSelected(false);
								TMP1 = "Windows";
								break;
																
								case 3:
								isDefaultL.setSelected(false);
								isWindowsL.setSelected(false);
								isNimbus.setSelected(true);
								TMP1 = "Nimbus";
								break;
							}
						}
					};
					isDefaultL.addActionListener(listener3);
					isWindowsL.addActionListener(listener3);
					isNimbus.addActionListener(listener3);	
					LAFOption.setLayout(new GridLayout(3,1,0,0));
					LAFOption.add(isDefaultL);
					LAFOption.add(isWindowsL);
					LAFOption.add(isNimbus);
					LAFOption.setSize(250,140);
					LAFOption.setLocationRelativeTo(w);
					LAFOption.setVisible(true);
					writeConfig("LAF", TMP1);
					JOptionPane.showMessageDialog(w, "The Look and Feel will be changed after restart.", "Done", JOptionPane.INFORMATION_MESSAGE);
					break;
					
					case 9: //other settings
					JDialog Settings = new JDialog(w);
					Settings.setModal(true);
					Settings.setTitle("Settings");
					Settings.getContentPane().setBackground(Color.WHITE);
					Settings.setLayout(new GridLayout(4,1,0,0));
					Settings.setSize(250,160);
					Settings.setLocationRelativeTo(w);
					boolean Portable = true;
					boolean CloseAfterLaunch = true;
					try
					{
						Portable = getConfig("isPortable").equals("true");
					}
					catch (Throwable ex)
					{
					}
					try
					{
						CloseAfterLaunch = getConfig("isCloseAfterLaunch").equals("true");
					}
					catch (Throwable ex)
					{
					}
					MyRadioButton isConfirmDrag = new MyRadioButton("Confirm Drag", confirmDrag, 0);
					MyRadioButton isPortable = new MyRadioButton("Replace drive letter", Portable, 1);
					MyRadioButton isLoadIcon = new MyRadioButton("Load icon (require restart)", Icon, 2);
					MyRadioButton isCloseAfterLaunch = new MyRadioButton("Hide window after launching", CloseAfterLaunch, 3);
					Settings.add(isConfirmDrag);
					Settings.add(isPortable);
					Settings.add(isLoadIcon);
					Settings.add(isCloseAfterLaunch);
					Settings.setVisible(true);
					confirmDrag = isConfirmDrag.isSelected();
					Icon = isLoadIcon.isSelected();
					writeConfig("ConfirmDrag", confirmDrag + "");
					writeConfig("isPortable", isPortable.isSelected() + "");
					writeConfig("isLoadIcon", Icon + "");
					writeConfig("isCloseAfterLaunch", isCloseAfterLaunch.isSelected() + "");
					break;
				}
			}
		}
		
		@Override
		public void mousePressed(MouseEvent ev)
		{
		}
		
		@Override
		public void mouseClicked(MouseEvent ev)
		{
		}
		
		@Override
		public void mouseEntered(MouseEvent ev)
		{
		}
		
		@Override
		public void mouseExited(MouseEvent ev)
		{
		}
	}
	
	class MyGrid extends JButton implements MouseListener
	{
		private int x;
		private final JPopupMenu popupMenu = new JPopupMenu();
		private final JMenuItem item = new JMenuItem("Delete");
		public MyGrid(int x)
		{
			this.setFont(f13);
			this.setBorder(bord);
			this.setFocusable(false);
			this.setBackground(Color.WHITE);
			this.setPreferredSize(new Dimension(50,50));
			gridPanel.add(this);
			this.addMouseListener(this);
			this.x = x;
			TMP1 = getConfig("Grid." + this.x);
			if (TMP1 != null)
			{
				if (!TMP1.isEmpty())
				{
					try
					{
						this.setIcon(getIcon32(new File(getConfig("Grid." + this.x))));
					}
					catch (Exception ex)
					{
					}
				}
			}
			popupMenu.add(item);
			item.setFont(f13);
			item.setBackground(Color.WHITE);
			item.addMouseListener(this);
			this.setDropTarget(new MyDropTarget(this.x));
		}
		
		private class MyDropTarget extends DropTarget
		{
			private int x;
			public MyDropTarget(int x)
			{
				this.x = x;
			}
			
			@Override
			public synchronized void drop(DropTargetDropEvent dtde)
			{
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				try
				{
					File file = (File)(((java.util.List)(dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor))).get(0));
					i = JOptionPane.showConfirmDialog(w, "Set " + file.getPath() + " to this grid?\nNote that the current shotcut will be overriden.", "Confirm", JOptionPane.YES_NO_OPTION);
					if (i == JOptionPane.YES_OPTION)
					{
						writeConfig("Grid." + x, file.getPath());
						setIcon(getIcon32(file));
					}
				}
				catch (Throwable ex)
				{
				}
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent ev)
		{
			if (ev.getSource() instanceof JButton)
			{
				if (ev.isPopupTrigger())
				{
					popupMenu.show((JButton)(ev.getSource()), ev.getX(), ev.getY());
				}
				else
				{
					if (getConfig("Grid." + this.x) == null)
					{
						i = chooser.showOpenDialog(w);
						if (i == JFileChooser.APPROVE_OPTION)
						{
							File f = chooser.getSelectedFile();
							writeConfig("Grid." + this.x, f.getPath());
							this.setIcon(getIcon32(f));
						}
					}
					else
					{
						File f = new File(getConfig("Grid." + this.x));
						try
						{
							openFile(f);
							final JButton button = (JButton)(ev.getSource());
							(new Thread()
							{
								@Override
								public void run()
								{
									try
									{
										for (i=0; i<3; i++)
										{
											this.sleep(500);
											button.setBackground(new Color(1,125,129));
											this.sleep(500);
											button.setBackground(Color.WHITE);
										}
										if (getConfig("isCloseAfterLaunch").equals("true"))
										{
											w.setVisible(false);
										}
									}
									catch (Throwable ex)
									{
										button.setBackground(Color.WHITE);
									}
								}
							}).start();
						}
						catch (Exception ex)
						{
							JOptionPane.showMessageDialog(w, "Cannot open " + f.getPath() + " !\nPlease make sure the path is correct and close portable mode if necessary.", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			else if (ev.getSource() instanceof JMenuItem)
			{
				this.setIcon(null);
				removeConfig("Grid." + this.x);
			}
		}
		
		@Override
		public void mousePressed(MouseEvent ev)
		{
		}
		
		@Override
		public void mouseClicked(MouseEvent ev)
		{
		}
		
		@Override
		public void mouseEntered(MouseEvent ev)
		{
			this.setBackground(new Color(240,240,240));
		}
		
		@Override
		public void mouseExited(MouseEvent ev)
		{
			this.setBackground(Color.WHITE);
		}
	}
	
	public void initialize()
	{
		if (!SettingsFile.exists())
		{
			try
			{
				PrintWriter writer = new PrintWriter(SettingsFile, "UTF-8");
				writer.close();
				writeConfig("Size.x", "280");
				writeConfig("Size.y", "700");
				writeConfig("Location.x", "0");
				writeConfig("Location.y", "0");
				writeConfig("OnTop", "false");
				writeConfig("LAF", "Default");
				writeConfig("ConfirmDrag", "true");
				writeConfig("isPortable", "false");
				writeConfig("isLoadIcon", "true");
				writeConfig("isCloseAfterLaunch", "true");
			}
			catch (Exception ex)
			{
			}
		}
	}
	
	class MyRadioButton extends JRadioButton
	{
		private int x;
		public MyRadioButton(String str, boolean isSelected, int x)
		{
			super(str, isSelected);
			this.setFont(f13);
			this.setBackground(Color.WHITE);
			this.setFocusable(false);
			this.x = x;
		}
		
		public int getIndex()
		{
			return this.x;
		}
	}
	
	public void saveSizeAndLocation()
	{
		writeConfig("Size.x", w.getSize().getWidth() + "");
		writeConfig("Size.y", w.getSize().getHeight() + "");
		writeConfig("Location.x", w.getLocation().getX() + "");
		writeConfig("Location.y", w.getLocation().getY() + "");
	}
	
	public void setSizeAndLocation()
	{
		this.setMinimumSize(new Dimension(280,700));
		try
		{
			i = (int)Double.parseDouble(getConfig("Size.x"));
			j = (int)Double.parseDouble(getConfig("Size.y"));
			if (i > WIDTH)
			{
				i = WIDTH;
			}		
			if (j > HEIGHT)
			{
				j = HEIGHT;
			}
		}
		catch (Exception ex)
		{
		}
		if ((i>=280)&&(j>=700))
		{
			this.setSize(i, j);
		}
		else
		{
			this.setSize(280,700);
		}
		
		//location
		try
		{
			k = (int)Double.parseDouble(getConfig("Location.x"));
			l = (int)Double.parseDouble(getConfig("Location.y"));
			if ((k+i) > WIDTH)
			{
				k = WIDTH-i;
			}
			if ((l+j) > HEIGHT)
			{
				l = HEIGHT-j;
			}
		}
		catch (Exception ex)
		{
		}	
		if ((k>=0)&&(l>=0))
		{
			this.setLocation(k, l);
		}
		else
		{
			this.setLocation(0, 0);
		}
		//ontop
		try
		{
			this.setAlwaysOnTop(getConfig("OnTop").equals("true"));
		}
		catch (Exception ex)
		{
			this.setAlwaysOnTop(true);
		}
		//confirm drag
		try
		{
			confirmDrag = !(getConfig("ConfirmDrag").equals("false"));
		}
		catch (Exception ex)
		{
			confirmDrag = true;
		}
		//load icon
		try
		{
			Icon = !(getConfig("isLoadIcon").equals("false"));
		}
		catch (Exception ex)
		{
			Icon = true;
		}
	}
	
	public ImageIcon getIcon32(File f)
	{
		try
		{
			return new ImageIcon(ShellFolder.getShellFolder(f).getIcon(true));
		}
		catch (Exception ex)
		{
			return null;
		}
	}
	
	public Icon getIcon16(File f)
	{
		return FileSystemView.getFileSystemView().getSystemIcon(f);
	}
	
	public void openFile(File f) throws IOException
	{
		Desktop.getDesktop().open(f);
	}
	
	public static File getJARPath()
	{
		try
		{			
			return new File((new File(JAppLaunch.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())).getParentFile().getPath());
		}
		catch (Exception ex)
		{
			return null;
		}
	}
	
	public int getNextUsable(String name)
	{
		java.util.List list = Collections.list(prop.propertyNames());
		for (i=0; i<list.size(); i++)
		{
			if (!list.contains(name + "." + i)) return i;
		}
		return i;
	}
	
	public static String getConfig(String name)
	{
		try
		{
			prop.load(new FileInputStream(SettingsFile));
			return prop.getProperty(name);
		}
		catch (Exception ex)
		{
			return null;
		}
	}
	
	public static void writeConfig(String key, String value)
	{
		prop.setProperty(key, value);
		try
		{
			prop.store(new FileOutputStream(SettingsFile), null);
		}
		catch (Exception ex)
		{
		}
	}
	
	public static void removeConfig(String key)
	{
		prop.remove(key);
		try
		{
			prop.store(new FileOutputStream(SettingsFile), null);
		}
		catch (Exception ex)
		{
		}
	}
}
