package com.javaweb.servlet;

import com.javaweb.model.MenuItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MenuServlet - Quản lý thực đơn (Xem, Thêm, Sửa, Xóa món ăn)
 */
@WebServlet("/menu")
public class MenuServlet extends HttpServlet {

    // Tạm lưu dữ liệu trong memory (cần thay bằng DB sau)
    private static final List<MenuItem> menuItems = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        super.init();
        // Khởi tạo menu items - 20 món chia 3 loại
        if (menuItems.isEmpty()) {
            int id = 1;
            
            // ========== MÓN ĂN (Cơm, Mì, Canh/Cơm truyền thống) ==========
            
            // Cơm
            addMenuItem(id++, "Cơm tấm sườn nướng", "Món ăn", 35000, 
                "Cơm tấm thơm ngon kèm sườn nướng, trứng ốp la và đồ chua.",
                "https://images.unsplash.com/photo-1504674900247-0877df9cc836?q=80&w=1500&auto=format&fit=crop");
            
            addMenuItem(id++, "Cơm chiên dương châu", "Món ăn", 40000,
                "Cơm chiên với trứng, tôm, cà rốt, hành và mềm thơm.",
                "https://images.unsplash.com/photo-1609335314336-da09dfb3f60d?q=80&w=1500&auto=format&fit=crop");
            
            addMenuItem(id++, "Cơm gà nước mắm", "Món ăn", 38000,
                "Cơm trắng với gà luộc, sốt nước mắm đặc biệt.",
                "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=1500&auto=format&fit=crop");
            
            addMenuItem(id++, "Cơm nước lèo", "Món ăn", 42000,
                "Cơm với nước lèo thơm, thịt heo nướng và rau ớt.",
                "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=1500&auto=format&fit=crop");
            
            // Mì
            addMenuItem(id++, "Mì Quảng", "Món ăn", 40000,
                "Mì Quảng đặc trưng Quảng Nam với nước dùng đậm đà và topping phong phú.",
                "https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?q=80&w=1500&auto=format&fit=crop");
            
            addMenuItem(id++, "Mì xào cay", "Món ăn", 35000,
                "Mì xào nóng với tôm, mực, rau và gia vị cay nồng.",
                "https://images.unsplash.com/photo-1612874742237-415221588de3?q=80&w=1500&auto=format&fit=crop");
            
            addMenuItem(id++, "Mì trộn lạnh", "Món ăn", 32000,
                "Mì lạnh trộn với sốt chua cay, rau sống và hạt vừng.",
                "https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?q=80&w=1500&auto=format&fit=crop");
            
            // Canh/Cơm truyền thống
            addMenuItem(id++, "Phở bò", "Món ăn nước", 45000,
                "Phở bò nước dùng đậm đà, thịt bò tái ngon mềm và hành lá thơm.",
                "https://images.unsplash.com/photo-1543353071-873f17a7a088?q=80&w=1500&auto=format&fit=crop");
            
            addMenuItem(id++, "Canh chua cá", "Món ăn nước", 38000,
                "Canh chua với cá tươi, cà chua, dứa và thơm ngon.",
                "https://images.unsplash.com/photo-1599599810694-5e2b0c80ebd7?q=80&w=1500&auto=format&fit=crop");
            
            addMenuItem(id++, "Bánh mì thịt", "Món ăn", 25000,
                "Bánh mì nóng giòn, pate béo ngậy, chả lụa và rau thơm.",
                "https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=1500&auto=format&fit=crop");
            
            addMenuItem(id++, "Gà rán giòn", "Món ăn", 55000,
                "Gà rán giòn rụm, bên trong vẫn giữ được độ thơm mềm và đậm vị.",
                "https://images.unsplash.com/photo-1548365328-55c0ae3d9999?q=80&w=1500&auto=format&fit=crop");
            
            // ========== ĐỒ UỐNG ==========
            
            addMenuItem(id++, "Trà đào cam sả", "Đồ uống", 20000,
                "Thức uống trái cây mát lạnh, tươi ngon và giải nhiệt mùa hè.",
                "https://images.unsplash.com/photo-1510626176961-4b66c4f54c83?q=80&w=1500&auto=format&fit=crop");
            
            addMenuItem(id++, "Nước cam ép tươi", "Đồ uống", 18000,
                "Nước cam ép tươi nguyên chất, giàu vitamin C.",
                "https://images.unsplash.com/photo-1506806732259-39c2d0268443?q=80&w=1500&auto=format&fit=crop");
            
            addMenuItem(id++, "Cà phê đen đậm", "Đồ uống", 12000,
                "Cà phê đen đậm, thơm ngon và tỉnh táo.",
                "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?q=80&w=1500&auto=format&fit=crop");
            
            addMenuItem(id++, "Cà phê sữa nóng", "Đồ uống", 14000,
                "Cà phê sữa nóng đậm đà, béo ngậy.",
                "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?q=80&w=1500&auto=format&fit=crop");
            
            addMenuItem(id++, "Sinh tố dâu", "Đồ uống", 22000,
                "Sinh tố dâu mát lạnh, mịn mủa với sữa chua.",
                "https://images.unsplash.com/photo-1520763185298-1b434c919eba?q=80&w=1500&auto=format&fit=crop");
            
            addMenuItem(id++, "Trà sữa trân châu", "Đồ uống", 25000,
                "Trà sữa ngon chuẩn vị Đài Loan với trân châu mềm.",
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?q=80&w=1500&auto=format&fit=crop");
            
            // ========== COMBO ==========
            
            addMenuItem(id++, "Combo 1: Phở + Trà đào", "Combo", 60000,
                "Phở bò + Trà đào cam sả, lựa chọn tuyệt vời cho bữa trưa.",
                "https://images.unsplash.com/photo-1543353071-873f17a7a088?q=80&w=1500&auto=format&fit=crop");
            
            addMenuItem(id++, "Combo 2: Cơm tấm + Cà phê", "Combo", 50000,
                "Cơm tấm sườn nướng + Cà phê đen, bộ đôi hoàn hảo.",
                "https://images.unsplash.com/photo-1504674900247-0877df9cc836?q=80&w=1500&auto=format&fit=crop");
            
            addMenuItem(id++, "Combo 3: Gà rán + Sinh tố", "Combo", 75000,
                "Gà rán giòn + Sinh tố dâu, bữa ăn ngon chuẩn vị.",
                "https://images.unsplash.com/photo-1548365328-55c0ae3d9999?q=80&w=1500&auto=format&fit=crop");
            
            addMenuItem(id++, "Combo 4: Mì Quảng + Nước cam", "Combo", 60000,
                "Mì Quảng + Nước cam ép tươi, kết hợp đặc sắc.",
                "https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?q=80&w=1500&auto=format&fit=crop");
        }
    }
    
    /**
     * Helper method to add menu items
     */
    private void addMenuItem(int id, String name, String category, double price, String description, String imageUrl) {
        MenuItem item = new MenuItem(name, category, price);
        item.setItemId(id);
        item.setDescription(description);
        item.setImageUrl(imageUrl);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        menuItems.add(item);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            showMenuList(request, response);
        } else if ("view".equals(action)) {
            int itemId = Integer.parseInt(request.getParameter("id"));
            MenuItem item = findMenuItemById(itemId);
            if (item != null) {
                request.setAttribute("menuItem", item);
                request.getRequestDispatcher("/views/canteen/menu_detail.jsp").forward(request, response);
            } else {
                response.sendError(404, "Menu item not found");
            }
        } else if ("add".equals(action)) {
            if (!isAdmin(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ admin mới có quyền thêm món ăn.");
                return;
            }
            request.setAttribute("pageTitle", "Thêm món ăn mới");
            request.setAttribute("formAction", "add");
            request.setAttribute("menuItem", new MenuItem());
            request.getRequestDispatcher("/views/canteen/menu_form.jsp").forward(request, response);
        } else if ("edit".equals(action)) {
            if (!isAdminOrStaff(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ admin hoặc nhân viên mới có quyền sửa món ăn.");
                return;
            }
            int itemId = Integer.parseInt(request.getParameter("id"));
            MenuItem item = findMenuItemById(itemId);
            request.setAttribute("menuItem", item);
            request.setAttribute("pageTitle", "Chỉnh sửa món ăn");
            request.setAttribute("formAction", "update");
            request.getRequestDispatcher("/views/canteen/menu_form.jsp").forward(request, response);
        } else if ("search".equals(action)) {
            // Handle search and category filter
            String query = request.getParameter("search");
            String category = request.getParameter("category");
            List<MenuItem> filtered = new ArrayList<>();
            String q = query != null ? query.trim().toLowerCase() : "";
            String c = category != null ? category.trim().toLowerCase() : "";
            for (MenuItem item : menuItems) {
                boolean matchesQuery = q.isEmpty() || (item.getItemName() != null && item.getItemName().toLowerCase().contains(q)) || (item.getDescription() != null && item.getDescription().toLowerCase().contains(q));
                boolean matchesCategory = c.isEmpty() || (item.getCategory() != null && item.getCategory().toLowerCase().equals(c));
                if (matchesQuery && matchesCategory) {
                    filtered.add(item);
                }
            }
            request.setAttribute("menuItems", filtered);
            request.setAttribute("canAdd", isAdmin(request));
            request.setAttribute("canEdit", isAdminOrStaff(request));
            request.setAttribute("canDelete", isAdminOrStaff(request));
            request.getRequestDispatcher("/views/canteen/menu.jsp").forward(request, response);
        } else {
            showMenuList(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");

        if ("add".equals(action)) {
            addMenuItem(request, response);
        } else if ("update".equals(action)) {
            updateMenuItem(request, response);
        } else if ("delete".equals(action)) {
            deleteMenuItem(request, response);
        } else {
            showMenuList(request, response);
        }
    }

    /**
     * Hiển thị danh sách menu
     */
    private void showMenuList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("menuItems", menuItems);
        request.setAttribute("canAdd", isAdmin(request));
        request.setAttribute("canEdit", isAdminOrStaff(request));
        request.setAttribute("canDelete", isAdminOrStaff(request));
        request.getRequestDispatcher("/views/canteen/menu.jsp").forward(request, response);
    }

    /**
     * Thêm menu item mới
     */
    private void addMenuItem(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ admin mới có quyền thêm món ăn.");
            return;
        }

        try {
            String itemName = request.getParameter("itemName");
            String description = request.getParameter("description");
            String category = request.getParameter("category");
            String imageUrl = request.getParameter("imageUrl");
            double price = Double.parseDouble(request.getParameter("price"));

            if (itemName == null || itemName.trim().isEmpty()) {
                request.setAttribute("error", "Tên món ăn không được trống!");
                showMenuList(request, response);
                return;
            }

            MenuItem item = new MenuItem(itemName, category, price);
            item.setDescription(description);
            item.setImageUrl(imageUrl != null && !imageUrl.trim().isEmpty()
                    ? imageUrl.trim()
                    : "https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?q=80&w=1500&auto=format&fit=crop");
            item.setItemId(menuItems.isEmpty() ? 1 : menuItems.get(menuItems.size() - 1).getItemId() + 1);
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            menuItems.add(item);

            request.setAttribute("success", "Thêm món ăn thành công!");
            showMenuList(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            showMenuList(request, response);
        }
    }

    /**
     * Cập nhật menu item
     */
    private void updateMenuItem(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdminOrStaff(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ admin hoặc nhân viên mới có quyền sửa món ăn.");
            return;
        }

        try {
            int itemId = Integer.parseInt(request.getParameter("id"));
            String itemName = request.getParameter("itemName");
            String description = request.getParameter("description");
            String category = request.getParameter("category");
            String imageUrl = request.getParameter("imageUrl");
            double price = Double.parseDouble(request.getParameter("price"));

            MenuItem item = findMenuItemById(itemId);
            if (item != null) {
                item.setItemName(itemName);
                item.setDescription(description);
                item.setCategory(category);
                item.setPrice(price);
                item.setImageUrl(imageUrl != null && !imageUrl.trim().isEmpty()
                        ? imageUrl.trim()
                        : item.getImageUrl());
                item.setUpdatedAt(LocalDateTime.now());
                request.setAttribute("success", "Cập nhật món ăn thành công!");
            } else {
                request.setAttribute("error", "Không tìm thấy món ăn!");
            }
            showMenuList(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            showMenuList(request, response);
        }
    }

    /**
     * Xóa menu item
     */
    private void deleteMenuItem(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdminOrStaff(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ admin hoặc nhân viên mới có quyền xóa món ăn.");
            return;
        }

        try {
            int itemId = Integer.parseInt(request.getParameter("id"));
            MenuItem item = findMenuItemById(itemId);
            if (item != null) {
                menuItems.remove(item);
                request.setAttribute("success", "Xóa món ăn thành công!");
            } else {
                request.setAttribute("error", "Không tìm thấy món ăn!");
            }
            showMenuList(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            showMenuList(request, response);
        }
    }

    /**
     * Tìm menu item theo ID
     */
    private MenuItem findMenuItemById(int itemId) {
        for (MenuItem item : menuItems) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    // Public accessor for other servlets (e.g., OrderServlet)
    public static java.util.List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public static MenuItem findMenuItemByIdStatic(int itemId) {
        for (MenuItem item : menuItems) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("role") != null;
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && "ADMIN".equals(session.getAttribute("role"));
    }

    private boolean isStaff(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && "STAFF".equals(session.getAttribute("role"));
    }

    private boolean isAdminOrStaff(HttpServletRequest request) {
        return isAdmin(request) || isStaff(request);
    }
}
