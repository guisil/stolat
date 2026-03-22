package app.stolat;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.boot.info.BuildProperties;
import org.springframework.lang.Nullable;

@AnonymousAllowed
public class MainLayout extends AppLayout {

    public MainLayout(@Nullable BuildProperties buildProperties) {
        var toggle = new DrawerToggle();
        var title = new H1("StoLat");
        title.addClassName("app-title");

        var navbar = new HorizontalLayout(toggle, title);
        navbar.setWidthFull();
        navbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        addToNavbar(navbar);

        var nav = new SideNav();
        nav.addItem(new SideNavItem("Birthdays", ""));
        nav.addItem(new SideNavItem("Collection", "collection"));
        nav.addItem(new SideNavItem("Missing", "missing-birthdays"));

        var drawerContent = new VerticalLayout(new Scroller(nav));
        drawerContent.setSizeFull();

        if (buildProperties != null) {
            var versionLabel = new Span("v" + buildProperties.getVersion());
            versionLabel.addClassName("version-label");
            drawerContent.add(versionLabel);
            drawerContent.setFlexGrow(1, nav);
        }

        addToDrawer(drawerContent);

        setPrimarySection(Section.DRAWER);
        setDrawerOpened(false);
    }
}
