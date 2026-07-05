package com.vfstr.smartclass.ui.screens.hierarchy

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.domain.models.LocalEnrollmentStudent
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem

data class HierarchyNode(
    val id: String,
    val name: String,
    val type: String,
    val children: List<HierarchyNode> = emptyList()
)

@OptIn(ExperimentalTextApi::class)
@androidx.lifecycle.viewmodel.compose.viewModel
@androidx.hilt.navigation.compose.hiltViewModel
@Composable
fun ScreenHierarchy(
    vm: MainViewModel,
    modifier: Modifier = Modifier,
    hierarchyVm: HierarchyViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val textMeasurer = rememberTextMeasurer()
    var viewMode by remember { mutableStateOf("Tree") } // Drilldown or Tree
    val rootNode by hierarchyVm.hierarchyRoot.collectAsState()
    
    LaunchedEffect(Unit) {
        hierarchyVm.loadHierarchy()
    }

    var selectedYear by remember { mutableStateOf("2025") }
    var selectedBranch by remember { mutableStateOf<String?>(null) }
    var selectedSection by remember { mutableStateOf<String?>(null) }
    
    var searchQuery by remember { mutableStateOf("") }

    val years = listOf("2025", "2024", "2023", "2022")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DesignSystem.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(DesignSystem.Padding)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = "Student Directory Hierarchy",
                        style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                    )
                    Text(text = "Drill-down by Admission Year, Branch and Section", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = { viewMode = if (viewMode == "Tree") "Drilldown" else "Tree" }) {
                    Icon(
                        imageVector = if (viewMode == "Tree") Icons.Default.AccountTree else Icons.Default.TableChart,
                        contentDescription = "Toggle View",
                        tint = DesignSystem.Cyan
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (viewMode == "Tree") {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(DesignSystem.CornerRadius))
                        .background(DesignSystem.Surface)
                        .border(1.dp, DesignSystem.Border, RoundedCornerShape(DesignSystem.CornerRadius))
                ) {
                    if (rootNode != null) {
                        HierarchyTreeCanvas(rootNode!!, textMeasurer)
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = DesignSystem.Cyan)
                        }
                    }
                }
            } else {
                // Drilldown Logic
                Column {
                    ScrollableTabRow(
                        selectedTabIndex = years.indexOf(selectedYear),
                        containerColor = Color.Transparent,
                        contentColor = DesignSystem.Cyan,
                        edgePadding = 0.dp,
                        divider = {}
                    ) {
                        years.forEach { year ->
                            Tab(
                                selected = selectedYear == year,
                                onClick = { 
                                    selectedYear = year
                                    selectedBranch = null
                                    selectedSection = null
                                },
                                text = { Text(year, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Global student search...", color = DesignSystem.TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = DesignSystem.TextMuted) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DesignSystem.Cyan,
                            unfocusedBorderColor = DesignSystem.Border
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        BreadcrumbItem("Root", selectedBranch != null || selectedSection != null) {
                            selectedBranch = null
                            selectedSection = null
                        }
                        if (selectedBranch != null) {
                            Icon(Icons.Default.ChevronRight, null, tint = DesignSystem.TextMuted, modifier = Modifier.size(16.dp))
                            BreadcrumbItem(selectedBranch!!, selectedSection != null) {
                                selectedSection = null
                            }
                        }
                        if (selectedSection != null) {
                            Icon(Icons.Default.ChevronRight, null, tint = DesignSystem.TextMuted, modifier = Modifier.size(16.dp))
                            BreadcrumbItem(selectedSection!!, false) {}
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedContent(
                        targetState = Triple(selectedBranch, selectedSection, searchQuery.isNotEmpty()),
                        label = "hierarchy_navigation",
                        transitionSpec = {
                            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                        }
                    ) { (branch, section, isSearching) ->
                        if (isSearching) {
                            SearchResultsList(searchQuery)
                        } else if (branch == null) {
                            BranchGrid { selectedBranch = it }
                        } else if (section == null) {
                            SectionGrid { selectedSection = it }
                        } else {
                            StudentLeafList(year = selectedYear, branch = branch, section = section)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreadcrumbItem(label: String, isClickable: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (isClickable) DesignSystem.Cyan else DesignSystem.TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        modifier = if (isClickable) Modifier.clickable { onClick() } else Modifier
    )
}

@Composable
fun BranchGrid(onSelect: (String) -> Unit) {
    val branches = listOf("CS", "AI", "CY", "DS", "CB", "IT", "BM", "EE", "EC", "ME", "CH", "CE")
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(branches) { b ->
            GlassmorphicCard(
                modifier = Modifier.clickable { onSelect(b) }
            ) {
                Column(Modifier.padding(DesignSystem.PaddingLarge), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, null, tint = DesignSystem.Cyan, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(b, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("180 Students", color = DesignSystem.TextSecondary, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun SectionGrid(onSelect: (String) -> Unit) {
    val sections = listOf("A", "B", "C", "D", "E")
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(sections) { s ->
            GlassmorphicCard(
                modifier = Modifier.clickable { onSelect(s) }
            ) {
                Column(Modifier.padding(DesignSystem.PaddingLarge), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Groups, null, tint = DesignSystem.Violet, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Section $s", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StudentLeafList(year: String, branch: String, section: String) {
    val students = (1..40).map { i ->
        LocalEnrollmentStudent("id_$i", "Student Name $i", "${year.takeLast(2)}L11A05${i.toString().padStart(2, '0')}", branch, year, section, "", "")
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Students in $year-$branch-$section", color = DesignSystem.TextSecondary, fontSize = 12.sp)
            IconButton(onClick = { /* Export */ }) {
                Icon(Icons.Default.FileDownload, null, tint = DesignSystem.Cyan, modifier = Modifier.size(20.dp))
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(students) { st ->
                GlassmorphicCard {
                    Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(DesignSystem.Surface), contentAlignment = Alignment.Center) {
                            Text(st.name.take(1), color = DesignSystem.Cyan, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(st.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(st.roll_number, color = DesignSystem.TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultsList(query: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No global results found for '$query'", color = DesignSystem.TextMuted)
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun HierarchyTreeCanvas(root: HierarchyNode, textMeasurer: TextMeasurer) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawHierarchyNode(root, 50f, center.y - 30f, 1, textMeasurer)
    }
}

@OptIn(ExperimentalTextApi::class)
fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHierarchyNode(
    node: HierarchyNode,
    x: Float,
    y: Float,
    level: Int,
    textMeasurer: TextMeasurer
) {
    val nodeWidth = 140f
    val nodeHeight = 50f
    val horizontalGap = 180f
    val verticalGap = 80f

    drawRoundRect(
        color = DesignSystem.Cyan.copy(alpha = 0.1f),
        topLeft = Offset(x, y),
        size = androidx.compose.ui.geometry.Size(nodeWidth, nodeHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
    )
    
    drawRoundRect(
        color = DesignSystem.Cyan,
        topLeft = Offset(x, y),
        size = androidx.compose.ui.geometry.Size(nodeWidth, nodeHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f),
        style = Stroke(width = 1f)
    )

    drawText(
        textMeasurer = textMeasurer,
        text = node.name,
        topLeft = Offset(x + 8f, y + 8f),
        style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = node.type,
        topLeft = Offset(x + 8f, y + 28f),
        style = TextStyle(color = DesignSystem.TextMuted, fontSize = 8.sp)
    )

    node.children.forEachIndexed { index, child ->
        val childX = x + horizontalGap
        val childY = y + (index * (nodeHeight + verticalGap)) - ((node.children.size - 1) * (nodeHeight + verticalGap) / 2)
        
        val path = Path().apply {
            moveTo(x + nodeWidth, y + nodeHeight / 2)
            cubicTo(
                x + nodeWidth + horizontalGap / 2, y + nodeHeight / 2,
                x + nodeWidth + horizontalGap / 2, childY + nodeHeight / 2,
                childX, childY + nodeHeight / 2
            )
        }
        drawPath(path, color = DesignSystem.Border, style = Stroke(width = 1f))
        
        drawHierarchyNode(child, childX, childY, level + 1, textMeasurer)
    }
}

@dagger.hilt.android.lifecycle.HiltViewModel
class HierarchyViewModel @javax.inject.Inject constructor(
    private val api: com.vfstr.smartclass.data.remote.api.RetrofitApi
) : androidx.lifecycle.ViewModel() {
    
    private val _hierarchyRoot = kotlinx.coroutines.flow.MutableStateFlow<HierarchyNode?>(null)
    val hierarchyRoot: kotlinx.coroutines.flow.StateFlow<HierarchyNode?> = _hierarchyRoot
    
    fun loadHierarchy() {
        androidx.lifecycle.viewModelScope.launch {
            try {
                val depts = api.getDepartments()
                val years = api.getYears()
                val sections = api.getSections()
                
                val deptNodes = depts.map { dept ->
                    HierarchyNode(dept, "$dept Dept", "Department", years.map { year ->
                        HierarchyNode("${dept}_$year", "Year $year", "Year", sections.map { sec ->
                            HierarchyNode("${dept}_${year}_$sec", "Section $sec", "Section")
                        })
                    })
                }
                
                val root = HierarchyNode(
                    "vfstr", "VFSTR University", "University",
                    listOf(HierarchyNode("cet", "College of Eng & Tech", "College", deptNodes))
                )
                
                _hierarchyRoot.value = root
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }
}

