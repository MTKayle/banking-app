# UI Home (BIDV-inspired) — Frontend-only Home Screen

This folder adds a brand-new Home screen built as a Fragment with modern, BIDV-inspired UX. It does not touch networking, API clients, or models.

Key features
- Account summary card with masked balance toggle
- Quick Actions grid (Transfer, QR Pay, Top-up, Bill pay, Cards, Savings, Loans, Insurance)
- Promotions carousel (reuses existing widget_carousel_v2)
- Services shortcuts
- Floating Action Button (speed-dial) for frequent actions
- Material spacing, colors, and accessibility basics

How to try it locally
1) Keep your current flows untouched. To open the new Home, you can either:
   - From any Activity, start UiHomeActivity:
     startActivity(new Intent(this, com.example.mobilebanking.ui_home.UiHomeActivity.class));
   - Or host the Fragment in an existing container:
     getSupportFragmentManager().beginTransaction()
         .replace(R.id.some_container, new com.example.mobilebanking.ui_home.HomeFragment())
         .commit();

2) Minimal integration (optional):
   - In CustomerDashboardActivity.java, you can navigate to UiHomeActivity from a temporary button/menu to evaluate the new UI.

No backend/API changes
- All data shown is from existing mock/data managers or is static placeholder UI.
- No changes to ApiClient or service interfaces.

Files added
- java: ui_home/UiHomeActivity.java, ui_home/HomeFragment.java
- layouts: ui_home_activity.xml, ui_home_fragment.xml, ui_home_view_account_card.xml, ui_home_view_quick_actions.xml, ui_home_view_txn_item.xml
- drawables: uihome_bg_primary_gradient.xml, uihome_card_bg.xml, uihome_fab_bg.xml, ic_eye_open.xml
- anim: anim_fade_in.xml, anim_slide_up.xml, anim_scale_in.xml, anim_fab_expand.xml, anim_fab_collapse.xml
- colors: uihome_* entries appended to values/colors.xml
- strings: added missing labels (qr_pay, cards, savings, loans, insurance)

Notes
- Icons for Savings/Loans/Insurance currently use neutral placeholders. Provide SVGs to replace them.
- If you share real BIDV screenshots/specs, we can update ordering/labels to match exactly.

