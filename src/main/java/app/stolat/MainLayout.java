package app.stolat;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
public class MainLayout extends AppLayout {

    public MainLayout() {
        var toggle = new DrawerToggle();
        var title = new H1("StoLat");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)");
        title.getStyle().set("margin", "0");

        var navbar = new HorizontalLayout(toggle, title);
        navbar.setWidthFull();
        navbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        addToNavbar(navbar);

        var nav = new SideNav();
        nav.addItem(new SideNavItem("Birthdays", "birthdays"));
        nav.addItem(new SideNavItem("Collection", "collection"));
        addToDrawer(new Scroller(nav));

        setPrimarySection(Section.DRAWER);
        setDrawerOpened(false);
    }
}
