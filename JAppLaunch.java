import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import myjava.gui.*;
import myjava.gui.common.Resources;

public class JAppLaunch extends JFrame implements Resources
{
	//constants
	private static final String VERSION_NO = "1.5";
	private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private static final int WIDTH = screenSize.width;
	private static final int HEIGHT = screenSize.height;
	private static final Color darkGreen = new Color(1,125,129);
	//settings
	private static final File settingFile = new File(getJARPath(), "JAPPLAUNCHPREFS.PROPERTIES");
	private static final Properties prop = new Properties();
	//component
	private final JTabbedPane tPane = new JTabbedPane();
	private final JPanel gridPanel = new JPanel();
	private final JMenuBar menuBar = new JMenuBar();
	private final JTextField searchBar = new JTextField();	
	private final HashSet<String> tabNames = new HashSet<>();
	private static MyWhiteFileChooser chooser = MyWhiteFileChooser.getInstance();
	private boolean confirmDrag = true;
	private boolean showIcon = true;
	private static JAppLaunch w;
	//tray
	private static final boolean useTray = SystemTray.isSupported();
	private static JPopupMenu popup;
	private static MyTrayIcon trayIcon;
	//
	public static void main(final String[] args)
	{
		final SplashScreen splash = SplashScreen.getSplashScreen();
		splash.createGraphics();
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				JAppLaunch.initialize();
				JAppLaunch.setLAF();
				w = new JAppLaunch("JAppLaunch " + VERSION_NO);
				Thread t1 = new Thread()
				{
					@Override
					public void run()
					{
						w.restoreMenu();
						w.restoreChooser();
					}
				};
				t1.start();				
				w.restoreTextField();
				/*
				 * load system tray
				 */
				if (useTray)
				{
					w.addIconToTray();
				}
				else
				{
					JOptionPane.showMessageDialog(w, "System tray is not supported!", "Error", JOptionPane.ERROR_MESSAGE);
				}
				try
				{
					t1.join();
				}
				catch (InterruptedException ex)
				{
					//pass
				}
				/*
				 * done
				 */
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
	
	public static void initialize()
	{
		if (!settingFile.exists())
		{
			try
			{
				PrintWriter writer = new PrintWriter(settingFile, "UTF-8");
				writer.close();
				writeConfig0("Settings.Size.x", "280");
				writeConfig0("Settings.Size.y", "700");
				writeConfig0("Settings.Location.x", "0");
				writeConfig0("Settings.Location.y", "0");
				writeConfig0("Settings.OnTop", "false");
				writeConfig0("Settings.LAF", "Default");
				writeConfig0("Settings.ConfirmDrag", "true");
				writeConfig0("Settings.isPortable", "false");
				writeConfig0("Settings.isLoadIcon", "true");
				writeConfig0("Settings.isCloseAfterLaunch", "true");
				saveConfig();
			}
			catch (Exception ex)
			{
			}
		}
		else
		{
			loadConfig();
		}
		UIManager.put("OptionPane.messageFont", f13);
		UIManager.put("OptionPane.buttonFont", f13);
		UIManager.put("OptionPane.yesButtonText", "YES");
		UIManager.put("OptionPane.noButtonText", "NO");
		UIManager.put("OptionPane.okButtonText", "OK");
		UIManager.put("Button.background", Color.WHITE);		
		UIManager.put("ToolTip.font", f13);
		UIManager.put("TextField.font",f13);
		UIManager.put("ToolTip.background", Color.WHITE);
		UIManager.put("PopupMenu.background", Color.WHITE);
		UIManager.put("ToolTip.border", bord1);
		ToolTipManager.sharedInstance().setInitialDelay(150);
	}
	
	public static void setLAF()
	{
		try
		{
			switch (getConfig0("Settings.LAF"))
			{
				case "Windows":
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				break;
				
				case "Nimbus":
				UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
				break;
			}
		}
		catch (Exception ex)
		{
		}
	}
	
	public JAppLaunch(String str)
	{
		super(str);	
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setSizeAndLocation();
		try
		{
			ArrayList<Image> img = new ArrayList<>();
			img.add(img("APPICON128"));
			img.add(img("APPICON48"));
			img.add(img("APPICON32"));
			img.add(img("APPICON16"));
			this.setIconImages(img);
		}
		catch (Exception ex)
		{
		}
		this.setJMenuBar(menuBar);
		this.setLayout(new BorderLayout());
		this.add(tPane, BorderLayout.CENTER);		
		//top panel
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(searchBar, BorderLayout.PAGE_START);
		//grid panel
		gridPanel.setLayout(new GridLayout(2,4,5,5));
		topPanel.add(gridPanel, BorderLayout.CENTER);
		this.add(topPanel, BorderLayout.PAGE_START);		
		//load icon
		showIcon = getBoolean0("Settings.isLoadIcon");
		load();
		restoreGrid();
	}
	
	public void restoreChooser()
	{
		tPane.setFont(f13);
		JComponent.setDefaultLocale(java.util.Locale.ENGLISH);
	}
	
	public void restoreTextField()
	{
		searchBar.setFont(f13);
		searchBar.setPreferredSize(new Dimension(0,26));
		searchBar.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent ev)
			{
				update();
			}
			
			@Override
			public void changedUpdate(DocumentEvent ev)
			{
				update();
			}
			
			@Override
			public void removeUpdate(DocumentEvent ev)
			{
				update();
			}
			
			public void update()
			{
				String text = searchBar.getText();
				JList myList = ((MyTab)(tPane.getSelectedComponent())).list;
				DefaultListModel model = (DefaultListModel)(myList.getModel());
				String tmp;
				//start searching:
				loadConfig();
				for (int i=0; i<model.size(); i++)
				{
					if ((tmp = getConfig0(model.getElementAt(i) + ".name")) == null)
					{
						tmp = (new File(getConfig0(model.getElementAt(i).toString()))).getName();
					}
					if (tmp.contains(text))
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
		gridPanel.add(new MyGrid(1));
		gridPanel.add(new MyGrid(2));
		gridPanel.add(new MyGrid(3));
		gridPanel.add(new MyGrid(4));
		gridPanel.add(new MyGrid(5));
		gridPanel.add(new MyGrid(6));
		gridPanel.add(new MyGrid(7));
		gridPanel.add(new MyGrid(8));
	}
	
	public void restoreMenu()
	{
		menuBar.setBackground(Color.WHITE);
		menuBar.setBorderPainted(false);
		MyMenu menu1 = new MyMenu("JAppLaunch");
		menu1.add(new MyMenuItem("Add new item to current tab", 1));
		menu1.add(new MyMenuItem("Add new tab", 2));
		menu1.add(new JSeparator());
		menu1.add(new MyMenuItem("Enable/disable always on top", 7));
		menu1.add(new MyStickMenu());
		menu1.add(new MyMenuItem("Set Look and Feel", 8));
		menu1.add(new MyMenuItem("Other settings", 9));
		if (System.getProperty("os.name").toLowerCase().startsWith("win"))
		{
			menu1.add(new MyWindowsMenu());
		}
		menu1.add(new JSeparator());
		if (useTray)
		{
			menu1.add(new MyMenuItem("Close to tray", 4));
		}
		menu1.add(new MyMenuItem("Close", 5));
		//
		MyMenu menu2 = new MyMenu("About");
		menu2.add(new MyMenuItem("About JAppLaunch", 6));
	}
	
	public void addIconToTray()
	{
		if (useTray)
		{
			popup = new JPopupMenu();
			trayIcon = new MyTrayIcon(img("APPICON48"), "JAppLaunch " + VERSION_NO, popup);
			trayIcon.setImageAutoSize(true);
			trayIcon.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseReleased(MouseEvent ev)
				{
					if (!ev.isPopupTrigger())
					{
						JAppLaunch.this.setVisible(!JAppLaunch.this.isVisible());
					}
				}
			});			
			popup.add(new MyMenuItem("Show/Hide", 10));
			popup.add(new MyMenuItem("About", "APPICON16", 6));
			popup.add(new JPopupMenu.Separator());
			popup.add(new MyMenuItem("Close", 5));
			try
			{
				SystemTray.getSystemTray().add(trayIcon);
			}
			catch (Exception ex)
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
		
		private MyMenuItem(String str, String icon, int x)
		{
			this(str,x);
			this.setIcon(icon(icon));
		}
		
		@Override
		public void mouseReleased(MouseEvent ev)
		{
			switch (x)
			{
				case 1: //add item
				loadConfig();
				int option = chooser.showOpenDialog(w);
				if (option == JFileChooser.APPROVE_OPTION)
				{
					File file = chooser.getSelectedFile();
					MyTab tab = (MyTab)(tPane.getSelectedComponent());
					String name = tab.getName();
					int next = getNextUsable(name);
					writeConfig0(name + "." + next, file.getPath());
					tab.list.insert(name + "." + next);
				}
				break;
				
				case 2: //add tab
				String name = JOptionPane.showInputDialog(JAppLaunch.this,"New tab name:","Input",JOptionPane.QUESTION_MESSAGE);
				if (name != null)
				{
					if (!name.isEmpty()&&(!tabNames.contains(name))&&(isUsableTypeName(name)))
					{
						MyTab tab = new MyTab(name);
					}
					else
					{
						JOptionPane.showMessageDialog(JAppLaunch.this, "The name \"" + name + "\" is invalid!", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				break;
				
				case 4: //close to tray
				JAppLaunch.this.setVisible(false);
				break;
				
				case 5: //close
				JAppLaunch.this.saveSizeAndLocation();
				System.exit(0);
				break;
				
				case 6: //about
				JAppLaunch.showAboutDialog();
				break;
				
				case 7: //always on top
				if (JAppLaunch.this.isAlwaysOnTop())
				{
					JAppLaunch.this.setAlwaysOnTop(false);
					writeConfig0("Settings.OnTop", "false");
				}
				else
				{
					JAppLaunch.this.setAlwaysOnTop(true);
					writeConfig0("Settings.OnTop", "true");
				}
				break;
				
				case 8: //look and feel
				JDialog LAFOption = new JDialog(JAppLaunch.this);
				LAFOption.setModal(true);
				LAFOption.setTitle("Look and Feel option");
				LAFOption.getContentPane().setBackground(Color.WHITE);
				boolean DefaultL = false;
				boolean WindowsL = false;
				boolean Nimbus = false;
				final String[] LAF = {getConfig0("Settings.LAF")};
				if (LAF[0] == null)
				{
					LAF[0] = "Default";
				}
				switch (LAF[0])
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
							LAF[0] = "Default";
							break;
							
							case 2:
							isDefaultL.setSelected(false);
							isWindowsL.setSelected(true);
							isNimbus.setSelected(false);
							LAF[0] = "Windows";
							break;
															
							case 3:
							isDefaultL.setSelected(false);
							isWindowsL.setSelected(false);
							isNimbus.setSelected(true);
							LAF[0] = "Nimbus";
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
				LAFOption.setLocationRelativeTo(JAppLaunch.this);
				LAFOption.setVisible(true);
				writeConfig0("Settings.LAF", LAF[0]);
				JOptionPane.showMessageDialog(w, "The Look and Feel will be changed after restart.", "Done", JOptionPane.INFORMATION_MESSAGE);
				break;
				
				case 9: //other settings
				{
					loadConfig();
					JDialog setting = new JDialog(w);
					setting.setModal(true);
					setting.setTitle("Settings");
					setting.getContentPane().setBackground(Color.WHITE);
					setting.setLayout(new GridLayout(4,1,0,0));
					setting.setSize(250,160);
					setting.setLocationRelativeTo(w);
					boolean Portable = getBoolean0("Settings.isPortable");
					boolean CloseAfterLaunch = getBoolean0("Settings.isCloseAfterLaunch");
					MyCheckBox isConfirmDrag = new MyCheckBox("Confirm Drag", confirmDrag);
					MyCheckBox isPortable = new MyCheckBox("Replace drive letter", Portable);
					MyCheckBox isLoadIcon = new MyCheckBox("Load icon", showIcon);
					MyCheckBox isCloseAfterLaunch = new MyCheckBox("Hide window after launching", CloseAfterLaunch);
					setting.add(isConfirmDrag);
					setting.add(isPortable);
					setting.add(isLoadIcon);
					setting.add(isCloseAfterLaunch);
					setting.setVisible(true);
					//
					confirmDrag = isConfirmDrag.isSelected();
					showIcon = isLoadIcon.isSelected();
					((MyTab)(tPane.getSelectedComponent())).list.lm.fireContentsChanged();
					writeConfig0("Settings.ConfirmDrag", confirmDrag + "");
					writeConfig0("Settings.isPortable", isPortable.isSelected() + "");
					writeConfig0("Settings.isLoadIcon", showIcon + "");
					writeConfig0("Settings.isCloseAfterLaunch", isCloseAfterLaunch.isSelected() + "");
					
				}
				break;
				
				case 10:
				JAppLaunch.this.setVisible(!JAppLaunch.this.isVisible());
				break;
			}
			saveConfig();
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
	
	class MyWindowsMenu extends JMenu
	{
		public MyWindowsMenu()
		{
			super("System utilities");
			this.setFont(f13);
			this.add(new MyWindowsMenuItem("Cancel all shutdown task(s)", 1));
			this.add(new JSeparator());
			this.add(new MyWindowsMenuItem("Registry editor", 2));
			this.add(new MyWindowsMenuItem("Paint", 3));
			this.add(new MyWindowsMenuItem("Clipboard", 4));
			this.add(new MyWindowsMenuItem("Calculator", 5));
			this.add(new MyWindowsMenuItem("Event viewer", 6));
			this.add(new MyWindowsMenuItem("Control panel", 7));
			this.add(new MyWindowsMenuItem("Task manager", 8));
		}
		
		private class MyWindowsMenuItem extends JMenuItem implements ActionListener
		{
			private int x;
			public MyWindowsMenuItem(String str, int x)
			{
				super(str);
				this.setForeground(Color.BLACK);
				this.setBackground(Color.WHITE);
				this.setFont(f13);
				this.x = x;
				this.addActionListener(this);
			}
			
			@Override
			public void actionPerformed(ActionEvent ev)
			{
				String cmd = null;
				switch (this.x)
				{
					case 1:
					cmd = "shutdown -a";
					break;
					
					case 2:
					cmd = "regedit";
					break;
					
					case 3:
					cmd = "mspaint";
					break;
					
					case 4:
					cmd = "clipbrd";
					break;
					
					case 5:
					cmd = "calc";
					break;
					
					case 6:
					cmd = "eventvwr";
					break;
					
					case 7:
					cmd = "control";
					break;
					
					case 8:
					cmd = "taskmgr";
					break;
				}
				try
				{
					Runtime.getRuntime().exec(cmd);
				}
				catch (Exception ex)
				{
				}
			}
		}
	}
	
	class MyStickMenu extends JMenu
	{
		MyStickMenu()
		{
			super("Stick to");
			this.setFont(f13);
			this.setBackground(Color.WHITE);
			this.add(new MyStickMenuItem("Left",1));
			this.add(new MyStickMenuItem("Right",2));
		}
		
		private class MyStickMenuItem extends JMenuItem implements ActionListener
		{
			private int x;
			private MyStickMenuItem(String str, int x)
			{
				super(str);
				this.setFont(f13);
				this.setBackground(Color.WHITE);
				this.x = x;
				this.addActionListener(this);
			}
			
			@Override
			public void actionPerformed(ActionEvent ev)
			{
				Rectangle env = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
				switch (this.x)
				{
					case 1: //left
					JAppLaunch.this.setLocation(0,0);
					JAppLaunch.this.setSize(280,env.height);
					break;
					
					case 2: //right
					JAppLaunch.this.setLocation(env.width-280,0);
					JAppLaunch.this.setSize(280,env.height);
					break;
				}
			}
		}
	}
	
	public void load()
	{
		/*
		 * Load lists from properties file
		 * assume that loadConfig() has been called
		 */
		File path = JAppLaunch.getJARPath();
		while (path.getParentFile() != null)
		{
			path = path.getParentFile();
		}
		String lastDrive = getConfig0("Settings.LastDrive");
		//path = root
		ArrayList<String> list = new ArrayList<>();
		/*
		 * now retain related items
		 */
		Set<String> set1 = prop.stringPropertyNames();
		for (String s: set1)
		{
			if (!(s.startsWith("Settings.")||s.startsWith("Grid.")))
			{
				list.add(s);
			}
		}
		/*
		 * remove special key for compatibility (version 1.4-)
		 */
		list.remove("Size.x");
		list.remove("Size.y");
		list.remove("Location.x");
		list.remove("Location.y");
		list.remove("OnTop");
		list.remove("LAF");
		list.remove("ConfirmDrag");
		list.remove("isPortable");
		list.remove("isLoadIcon");
		list.remove("isCloseAfterLaunch");
		list.remove("LastDrive");
		Collections.sort(list, new StringComparator());
		final boolean replaceDriveLetter = getBoolean0("Settings.isPortable");
		for (ArrayList<String> set: JAppLaunch.categorize(list))
		{
			//now set has items of the same type
			String first = set.get(0);
			String type = first.substring(0,first.indexOf("."));
			MyTab tab = null;
			if (tabNames.contains(type))
			{
				//search
				for (int i=0; i<tPane.getTabCount(); i++)
				{
					MyTab t = (MyTab)(tPane.getComponentAt(i));
					if (t.getName().equals(type))
					{
						tab = t;
						break;
					}
				}
			}
			else
			{
				//create new
				tab = new MyTab(type);
			}
			for (ArrayList<String> l: split(set))
			{
				for (String item: l)
				{
					if (!item.endsWith("name"))
					{
						try
						{
							//replace drive letter
							if (replaceDriveLetter)
							{
								if (!path.getPath().equals(lastDrive))
								{
									writeConfig0(item, getConfig0(item).replace(lastDrive, path.getPath()));
								}
							}
						}
						catch (Exception ex)
						{
						}
						tab.list.lm.addElement(item);
					}
				}				
			}
		}
		if (tPane.getTabCount() == 0)
		{
			//no items
			MyTab tab = new MyTab("Programs");
		}
		writeConfig0("Settings.LastDrive", path.getPath());
		saveConfig();
	}
	
	static ArrayList<ArrayList<String>> categorize(ArrayList<String> set)
	{
		ArrayList<ArrayList<String>> _return = new ArrayList<>();
		HashSet<String> types = new HashSet<>();
		for (String s: set)
		{
			types.add(s.substring(0,s.indexOf(".")));
		}
		ArrayList<String> typeList = new ArrayList<>(types);
		for (int i=0; i<typeList.size(); i++)
		{
			_return.add(new ArrayList<String>());
		}
		Collections.sort(typeList);
		if (typeList.contains("Programs"))
		{
			typeList.remove("Programs");
			typeList.add(0,"Programs");
		}
		//now add String to different ArrayList<String>
		for (String s: set)
		{
			for (int i=0; i<typeList.size(); i++)
			{
				if (s.startsWith(typeList.get(i)))
				{
					_return.get(i).add(s);
					break;
				}
			}
		}
		return _return;
	}
	
	static ArrayList<ArrayList<String>> split(java.util.List<String> list)
	{
		//split to 2 lists: dir and file
		ArrayList<String> l1 = new ArrayList<>();
		ArrayList<String> l2 = new ArrayList<>();
		for (String s: list)
		{
			if (new File(getConfig0(s)).isDirectory())
			{
				l1.add(s);
			}
			else l2.add(s);
		}
		ArrayList<ArrayList<String>> l = new ArrayList<>();
		l.add(l1);
		l.add(l2);
		return l;
	}
	
	class StringComparator implements Comparator<String>
	{
		@Override
		public int compare(String str1, String str2)
		{
			String s1, s2;
			if ((s1 = getConfig0(str1 + ".name")) == null)
			{
				s1 = (new File(getConfig0(str1))).getName();
			}
			if ((s2 = getConfig0(str2 + ".name")) == null)
			{
				s2 = (new File(getConfig0(str2))).getName();
			}
			return s1.compareTo(s2);
		}
	}
	
	class MyTab extends JPanel implements MouseListener
	{
		final MyList list = new MyList();		
		final JPopupMenu popup = new JPopupMenu();
		final JPanel base = new JPanel(new FlowLayout(FlowLayout.CENTER,2,0));
		final MyLabel label;
		String tabName;
		public MyTab(String tabName)
		{
			this.setLayout(new BorderLayout());
			this.setBackground(Color.WHITE);
			this.setDropTarget(new MyDropTarget());
			tPane.addTab(tabName, null, this, null);
			this.add(new JScrollPane(list), BorderLayout.CENTER);
			//create close popup
			this.popup.add(new MyTabMenuItem("Close tab",-1));
			this.popup.add(new MyTabMenuItem("Rename tab",-2));
			this.popup.add(new MyTabMenuItem("Sort",-3));
			base.setOpaque(false);
			label = new MyLabel(tabName);			
			base.add(label);
			base.setFocusable(false);
			base.addMouseListener(this);
			tPane.setTabComponentAt(tPane.indexOfComponent(this),this.base);
			//
			this.tabName = tabName;
			tabNames.add(tabName);
		}
		
		@Override
		public void mouseReleased(MouseEvent ev)
		{
			Object src = ev.getSource();
			if (src instanceof JPanel)
			{
				if (ev.isPopupTrigger())
				{					
					this.popup.show(base,ev.getX(),ev.getY());					
				}
				else
				{
					/*
					 * adding MouseListener will disable tab swap
					 * so implements it manually
					 */
					if (ev.getClickCount() == 1)
					{
						MouseEvent me = SwingUtilities.convertMouseEvent(base,ev,tPane);
						tPane.getMouseListeners()[0].mousePressed(me);
					}
					else
					{
						//rename
						new MyTabMenuItem("",-2).actionPerformed(null);
					}
				}
			}
		}
		
		public String getName()
		{
			return this.tabName;
		}
		
		public void setName(String name)
		{
			this.tabName = name;
			this.label.setText(name);
			tPane.repaint();
		}
		
		class MyList extends JList<String> implements MouseListener
		{
			private final MyListModel lm = new MyListModel();
			private final JPopupMenu popupMenu = new JPopupMenu();
			private class MyListModel extends DefaultListModel<String>
			{
				public MyListModel()
				{
					super();
				}
				
				public void fireContentsChanged()
				{
					this.fireContentsChanged(list, 0, this.size());
				}
			}			
			public MyList()
			{
				super();
				this.setModel(lm);
				this.setFixedCellHeight(22);
				this.popupMenu.add(new MyTabMenuItem("Delete", 1));
				this.popupMenu.add(new MyTabMenuItem("Set preferred name", 2));
				this.popupMenu.add(new MyTabMenuItem("Set parameters", 3));
				this.popupMenu.add(new MyTabMenuItem("Show path",4));
				this.addMouseListener(this);
				this.setDragEnabled(true);
				this.setCellRenderer(new DefaultListCellRenderer()
				{
					@Override
					public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
					{
						JLabel c = (JLabel)(super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus));
						c.setFont(f13);
						File file = new File(getConfig0((String)value));
						// load icon
						if (showIcon)
						{
							c.setIcon(getIcon16(file));
						}
						// load preferred name
						String s;
						if ((s = getConfig0(value + ".name")) != null)
						{
							c.setText(s);
						}
						else
						{
							c.setText(file.getName());
						}
						// selected/not selected
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
						int i = list.getSelectedIndex();
						if (i >= 0)
						{
							loadConfig();
							String f = (String)(this.lm.getElementAt(i));
							try
							{
								launch(f);
								(new Thread()
								{
									/*
									 * small "opening" window
									 */
									@Override
									public void run()
									{
										final JDialog window = new JDialog(JAppLaunch.this);
										JLabel label = new JLabel("   Opening...   ");
										label.setFont(f13);
										window.setUndecorated(true);
										window.getContentPane().setBackground(new Color(252,255,220));
										window.getRootPane().setBorder(bord1);
										window.setLayout(new FlowLayout(FlowLayout.CENTER));
										window.add(label);
										window.pack();
										window.setLocationRelativeTo(w);										
										window.addWindowListener(new WindowAdapter()
										{
											@Override
											public void windowDeactivated(WindowEvent ev)
											{
												window.dispose();
											}
										});
										window.setVisible(true);
										try
										{
											this.sleep(2000);
										}
										catch (Throwable ex)
										{
										}
										window.dispose();
										if (getBoolean0("Settings.isCloseAfterLaunch"))
										{
											JAppLaunch.this.setVisible(false);
										}
									}
								}).start();
							}
							catch (Exception ex)
							{
								JOptionPane.showMessageDialog(JAppLaunch.this, "Cannot open " + getConfig0(f) + " !\nPlease make sure the path is correct and close portable mode if necessary.", "Error", JOptionPane.ERROR_MESSAGE);
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
			
			void sort()
			{
				ArrayList<String> _list = Collections.list(this.lm.elements());
				Collections.sort(_list, new StringComparator());
				ArrayList<ArrayList<String>> lists = split(_list);
				lm.removeAllElements();
				for (ArrayList<String> list: lists)
				{
					for (String str: list)
					{
						lm.addElement(str);
					}
				}
				this.repaint();
			}
			
			void insert(String s)
			{
				ArrayList<String> list = Collections.list(this.lm.elements());
				list.add(s);
				Collections.sort(list, new StringComparator());
				ArrayList<ArrayList<String>> lists = split(list);
				//
				ArrayList<String> list0 = lists.get(0);
				list0.addAll(lists.get(1));
				int index = list0.indexOf(s);
				this.lm.add(index, s);
				this.setSelectedIndex(index);
				this.ensureIndexIsVisible(index);
			}
		}
		
		class MyTabMenuItem extends JMenuItem implements ActionListener
		{
			private int x;
			public MyTabMenuItem(String str, int x)
			{
				super(str);
				this.setFont(f13);
				this.setBackground(Color.WHITE);
				this.addActionListener(this);
				this.x = x;
			}
			
			@Override
			public void actionPerformed(ActionEvent ev)
			{
				loadConfig();
				switch (x)
				{
					case -1: //remove tab
					{
						int option = JOptionPane.showConfirmDialog(JAppLaunch.this,"<html>This will close the tab " + MyTab.this.tabName + " and all links will be <b>removed</b>.<br>Continue?</html>","Confirm",JOptionPane.YES_NO_OPTION);
						if (option == JOptionPane.YES_OPTION)
						{
							//now remove tab and properties
							tPane.remove(MyTab.this);
							tPane.repaint();
							loadConfig();
							for (String key: prop.stringPropertyNames())
							{
								if (key.startsWith(MyTab.this.tabName))
								{
									removeConfig0(key);
								}
							}
							saveConfig();
						}
						break;
					}
					
					case -2: //rename tab
					{
						String input = (String)(JOptionPane.showInputDialog(JAppLaunch.this, "Please enter the preferred name:", "Set preferred name", JOptionPane.QUESTION_MESSAGE, null, null, MyTab.this.tabName));
						if (input != null)
						{
							if ((!input.isEmpty())&&(isUsableTypeName(input)))
							{
								loadConfig();
								ArrayList<String> elements = Collections.list(MyTab.this.list.lm.elements());
								MyTab.this.list.lm.removeAllElements();
								//replace properties
								for (String key: prop.stringPropertyNames())
								{
									if (key.startsWith(MyTab.this.tabName))
									{
										String newKey = key.replace(MyTab.this.tabName,input);
										writeConfig0(newKey, removeConfig0(key));
									}
								}
								//add back elements
								for (String key: elements)
								{
									MyTab.this.list.lm.addElement(key.replace(MyTab.this.tabName,input));
								}
								saveConfig();
								MyTab.this.setName(input);
								MyTab.this.repaint();
							}
							else
							{
								JOptionPane.showMessageDialog(JAppLaunch.this, "The name \"" + input + "\" is invalid!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
					break;
					
					case -3: //sort					
					MyTab.this.list.sort();
					break;
					
					case 1: //delete
					{
						int array[] = MyTab.this.list.getSelectedIndices();
						int i = 0;
						if (array != null)
						{
							for (int x: array)
							{
								if (x >= 0)
								{
									String item = (String)(MyTab.this.list.lm.getElementAt(x-i));
									MyTab.this.list.lm.removeElementAt(x-i);
									removeConfig0(item);
									removeConfig0(item + ".name");
									removeConfig0(item + ".parameters");
								}
								i++;
							}
						}
					}
					break;
					
					case 2: //preferred name
					{
						int i = MyTab.this.list.getSelectedIndex();
						if (i >= 0)
						{
							String item = getConfig0(MyTab.this.list.lm.getElementAt(i) + ".name");
							if (item == null)
							{
								item = getConfig0(MyTab.this.list.lm.getElementAt(i));
								if (item != null)
								{
									item = new File(item).getName();
								}
							}
							String input = (String)(JOptionPane.showInputDialog(JAppLaunch.this, "Please enter the preferred name:", "Set preferred name", JOptionPane.QUESTION_MESSAGE, null, null, item));
							if (input != null)
							{							
								if (!input.isEmpty())
								{								
									if (i >= 0)
									{
										writeConfig0(MyTab.this.list.lm.getElementAt(i) + ".name", input);
									}
								}
								else
								{
									removeConfig0(MyTab.this.list.lm.getElementAt(i) + ".name");
								}
							}
						}
						MyTab.this.list.lm.fireContentsChanged();
					}
					break;
					
					case 3: //parameters
					{
						String key = MyTab.this.list.getSelectedValue() + ".parameters";
						String input = (String)JOptionPane.showInputDialog(JAppLaunch.this, "Please enter the parameters:", "Set parameters", JOptionPane.QUESTION_MESSAGE, null, null, getConfig0(key));
						if (input != null)
						{
							writeConfig0(key,input);
						}
					}
					break;
					
					case 4: //show path
					{
						String key = MyTab.this.list.getSelectedValue();
						String path = getConfig0(key);
						String par = getConfig0(key + ".parameters");
						String name = getConfig0(key + ".name");
						JOptionPane.showMessageDialog(JAppLaunch.this,"Name: " + (name==null?(new File(path).getName()):name) + "\nPath: " + path + (par!=null?((par.isEmpty()?"":"\nParameters: ")+par):""),"Information",JOptionPane.INFORMATION_MESSAGE);
					}
					break;
				}
				saveConfig();
			}
		}
		
		class MyDropTarget extends DropTarget
		{
			public MyDropTarget()
			{
				super();
			}
			
			@Override
			public synchronized void drop(DropTargetDropEvent dtde)
			{
				loadConfig();
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);			
				try
				{
					java.util.List list = (java.util.List)(dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
					for (Object obj: list)
					{
						if (obj instanceof File)
						{
							File file = (File)obj;
							MyTab tab = (MyTab)(tPane.getSelectedComponent());
							String typeName = tab.getName();
							int option;
							if (confirmDrag)
							{
								option = JOptionPane.showConfirmDialog(JAppLaunch.this, "Add " + file.getPath() + " to " + typeName + "?", "Confirm", JOptionPane.YES_NO_OPTION);
							}
							else option = JOptionPane.YES_OPTION;
							if (option == JOptionPane.YES_OPTION)
							{
								int next = getNextUsable(typeName);
								writeConfig0(typeName + "." + next, file.getPath());
								writeConfig0(typeName + "." + next + ".name", file.getName());
								removeConfig0(typeName + "." + next + ".parameters");
								tab.list.insert(typeName + "." + next);
							}
						}
					}
					saveConfig();
				}
				catch (Exception ex)
				{
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
		
		@Override
		public void mousePressed(MouseEvent ev)
		{
		}
		
		@Override
		public void mouseClicked(MouseEvent ev)
		{
		}
	}
	
	class MyGrid extends JButton implements MouseListener
	{
		private int x;
		private final JPopupMenu popupMenu = new JPopupMenu();
		public MyGrid(int x)
		{
			this.setFont(f13);
			this.setBorder(bord1);
			this.setFocusable(false);
			this.setBackground(Color.WHITE);
			this.setPreferredSize(new Dimension(50,50));			
			this.addMouseListener(this);
			this.x = x;
			popupMenu.add(new MyGridMenuItem("Delete", 1));
			popupMenu.add(new MyGridMenuItem("Set tooltip text", 2));
			popupMenu.add(new MyGridMenuItem("Set parameters", 3));
			popupMenu.add(new MyGridMenuItem("Show path",4));
			this.loadToolTipText();
			this.setDropTarget(new MyDropTarget());
			//
			try
			{
				this.setIcon(getIcon32(new File(getConfig0("Grid." + this.x))));
			}
			catch (Exception ex)
			{
				this.setIcon(null);
			}
		}
		
		public void loadToolTipText()
		{
			loadConfig();
			String tooltip = getConfig0("Grid." + this.x + ".name");
			if (tooltip == null)
			{
				String fileName = getConfig0("Grid." + this.x);
				if (fileName != null)
				{
					tooltip = (new File(fileName)).getName();
				}
				else
				{
					tooltip = getConfig0("Grid." + this.x);
				}
			}
			super.setToolTipText(tooltip);
		}
		
		private class MyGridMenuItem extends JMenuItem implements ActionListener
		{
			private int index;
			public MyGridMenuItem(String str, int index)
			{
				super(str);
				this.setFont(f13);
				this.setBackground(Color.WHITE);
				this.addActionListener(this);
				this.index = index;
			}
			
			@Override
			public void actionPerformed(ActionEvent ev)
			{
				loadConfig();
				switch (this.index)
				{
					case 1: //delete
					MyGrid.this.setIcon(null);
					removeConfig0("Grid." + MyGrid.this.x);
					removeConfig0("Grid." + MyGrid.this.x + ".name");
					removeConfig0("Grid." + MyGrid.this.x + ".parameters");
					MyGrid.this.setToolTipText(null);
					break;
					
					case 2: //tooltiptext
					{
						String input = (String)(JOptionPane.showInputDialog(JAppLaunch.this, "Please enter the preferred name:", "Set preferred name", JOptionPane.QUESTION_MESSAGE, null, null, MyGrid.this.getToolTipText()));
						if (input != null)
						{
							if (input.isEmpty())
							{
								removeConfig0("Grid." + MyGrid.this.x + ".name");								
							}
							else
							{
								writeConfig0("Grid." + MyGrid.this.x + ".name", input);
							}
							//have to saveConfig() before loading tooltip text
							saveConfig();
							MyGrid.this.loadToolTipText();
						}
					}
					break;
					
					case 3: //parameter
					{
						String key = "Grid." + MyGrid.this.x + ".parameters";
						String input = (String)JOptionPane.showInputDialog(JAppLaunch.this, "Please enter the parameters:", "Set parameters", JOptionPane.QUESTION_MESSAGE, null, null, getConfig0(key));
						if (input != null)
						{
							writeConfig0(key, input);
						}
					}
					break;
					
					case 4: //show path
					{
						String key = "Grid." + MyGrid.this.x;
						String path = getConfig0(key);
						String par = getConfig0(key + ".parameters");
						String name = getConfig0(key + ".name");
						JOptionPane.showMessageDialog(JAppLaunch.this,"Name: " + (name==null?(new File(path).getName()):name) + "\nPath: " + path + (par!=null?((par.isEmpty()?"":"\nParameters: ")+par):""),"Information",JOptionPane.INFORMATION_MESSAGE);
					}
					break;
				}
				saveConfig();
			}
		}
		
		private class MyDropTarget extends DropTarget
		{
			public MyDropTarget()
			{
				super();
			}
			
			@Override
			public synchronized void drop(DropTargetDropEvent dtde)
			{
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				try
				{
					File file = (File)(((java.util.List)(dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor))).get(0));
					int option = JOptionPane.showConfirmDialog(JAppLaunch.this, "Set " + file.getPath() + " to this grid?\nNote that the current shotcut will be overriden.", "Confirm", JOptionPane.YES_NO_OPTION);
					if (option == JOptionPane.YES_OPTION)
					{
						writeConfig0("Grid." + MyGrid.this.x, file.getPath());
						writeConfig0("Grid." + MyGrid.this.x + ".name", file.getName());
						writeConfig0("Grid." + MyGrid.this.x + ".parameters", "");
						saveConfig();
						MyGrid.this.setIcon(getIcon32(file));
						MyGrid.this.loadToolTipText();						
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent ev)
		{
			Object comp = ev.getSource();
			if (comp instanceof JButton)
			{
				if (ev.isPopupTrigger())
				{
					popupMenu.show((JButton)comp, ev.getX(), ev.getY());
				}
				else
				{					
					if (getConfig0("Grid." + this.x) == null)
					{
						/*
						 * set file to grid
						 */
						int option = chooser.showOpenDialog(JAppLaunch.this);
						if (option == JFileChooser.APPROVE_OPTION)
						{
							File f = chooser.getSelectedFile();
							loadConfig();
							writeConfig0("Grid." + this.x, f.getPath());
							MyGrid.this.setIcon(getIcon32(f));
							MyGrid.this.loadToolTipText();
							saveConfig();
						}
					}
					else
					{
						String f = "Grid." + MyGrid.this.x;
						try
						{
							launch(f);
							final JButton button = (JButton)(ev.getSource());
							//animation
							(new Thread()
							{
								@Override
								public void run()
								{
									try
									{
										for (int i=0; i<3; i++)
										{
											this.sleep(500);
											button.setBackground(darkGreen);
											this.sleep(500);
											button.setBackground(Color.WHITE);
										}
										if (getBoolean0("isCloseAfterLaunch"))
										{
											this.sleep(500);
											JAppLaunch.this.setVisible(false);
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
							JOptionPane.showMessageDialog(JAppLaunch.this, "Cannot open " + getConfig0(f) + " !\nPlease make sure the path is correct and close portable mode if necessary.", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
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
			this.setBackground(new Color(240,240,240));
		}
		
		@Override
		public void mouseExited(MouseEvent ev)
		{
			this.setBackground(Color.WHITE);
		}
	}
	
	public void saveSizeAndLocation()
	{
		loadConfig();
		Dimension d = this.getSize();
		Point p = this.getLocation();
		writeConfig0("Settings.Size.x", d.width + "");
		writeConfig0("Settings.Size.y", d.height + "");
		writeConfig0("Settings.Location.x", p.x + "");
		writeConfig0("Settings.Location.y", p.y + "");
		saveConfig();
	}
	
	public void setSizeAndLocation()
	{
		//size
		this.setMinimumSize(new Dimension(280,500));
		int width=0,height=0;
		try
		{
			width = (int)Double.parseDouble(getConfig0("Settings.Size.x"));
			height = (int)Double.parseDouble(getConfig0("Settings.Size.y"));
			width = Math.max(Math.min(width,WIDTH),280);
			height = Math.max(Math.min(height,HEIGHT),500);			
		}
		catch (Exception ex)
		{
			width = 280;
			height = 500;
		}
		finally
		{
			this.setSize(width, height);
		}
		//location
		try
		{
			int x = (int)Double.parseDouble(getConfig0("Settings.Location.x"));
			int y = (int)Double.parseDouble(getConfig0("Settings.Location.y"));
			x = Math.max(0,Math.min(x,WIDTH-width));
			y = Math.max(0,Math.min(y,WIDTH-width));
			this.setLocation(x,y);
		}
		catch (Exception ex)
		{
			this.setLocation(0,0);
		}
		//
		this.setAlwaysOnTop(getBoolean0("Settings.OnTop"));
		confirmDrag = getBoolean0("Settings.ConfirmDrag");
	}
	
	public static Icon getIcon32(File f)
	{
		try
		{
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
			return new ImageIcon((Image)o2);
		}
		catch (Exception ex)
		{
			return getIcon16(f);
		}
	}
	
	public static Icon getIcon16(File f)
	{
		return FileSystemView.getFileSystemView().getSystemIcon(f);
	}
	
	public static void openFile(File f) throws IOException
	{
		Desktop.getDesktop().open(f);
	}
	
	public static void launch(String key) throws IOException
	{
		loadConfig();
		String value = getConfig0(key);
		String par = getConfig0(key + ".parameters");
		if (par == null)
		{
			openFile(new File(value));
		}
		else if (par.isEmpty())
		{
			openFile(new File(value));
		}
		else
		{
			new ProcessBuilder(value,par).start();
		}
	}
	
	public static void showAboutDialog()
	{
		Icon ico;
		try
		{
			ico = icon("APPICON48");
		}
		catch (Exception ex)
		{
			ico = null;
		}
		JOptionPane.showMessageDialog(w, "JAppLaunch " + VERSION_NO + " -- a file launcher written in Java.\nBy tony200910041, http://tony200910041.wordpress.com\nDistributed under MPL 2.0.\nOfficial website: http://japplaunch.sourceforge.net/", "About JAppLaunch", JOptionPane.INFORMATION_MESSAGE, ico);
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
	
	public static int getNextUsable(String name)
	{
		ArrayList list = Collections.list(prop.propertyNames());
		int i=0;
		while (true)
		{
			if (!list.contains(name + "." + i)) return i;
			else i++;
		}
	}
	
	public static boolean isUsableTypeName(String name)
	{
		ArrayList<String> reserved = new ArrayList<>(14);
		reserved.add("Programs");
		reserved.add("Grid");
		reserved.add("Settings");
		reserved.add("Size.x");
		reserved.add("Size.y");
		reserved.add("Location.x");
		reserved.add("Location.y");
		reserved.add("OnTop");
		reserved.add("LAF");
		reserved.add("ConfirmDrag");
		reserved.add("isPortable");
		reserved.add("isLoadIcon");
		reserved.add("isCloseAfterLaunch");
		reserved.add("LastDrive");
		return !reserved.contains(name);
	}
	
	public static ImageIcon icon(String name)
	{
		return new ImageIcon(JAppLaunch.class.getResource("myjava/SRC/" + name + ".PNG"));
	}
	
	public static Image img(String name)
	{
		return icon(name).getImage();
	}
	
	public static void loadConfig()
	{
		try
		{
			prop.load(new FileInputStream(settingFile));
		}
		catch (Exception ex)
		{
		}
	}
	
	public static String getConfig0(String name)
	{
		return prop.getProperty(name);
	}
	
	public static boolean getBoolean0(String name)
	{
		return ("true").equals(getConfig0(name));
	}
	
	public static void writeConfig0(String key, String value)
	{
		prop.setProperty(key, value);
	}
	
	public static String removeConfig0(String key)
	{
		return (String)(prop.remove(key));
	}
	
	public static void saveConfig()
	{
		try
		{
			prop.store(new FileOutputStream(settingFile), null);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}	
	/*
	 * direct method:
	 */
	public static boolean getBoolean(String name)
	{
		loadConfig();
		return getBoolean0(name);
	}
	
	public static String getConfig(String name)
	{
		loadConfig();
		return getConfig0(name);
	}
	
	public static void writeConfig(String key, String name)
	{
		writeConfig0(key,name);
		saveConfig();
	}
}
