# Guía: Cómo usar ECharts en tu Proyecto

## 1. Ya lo tienes instalado ✅
ECharts ya está en tu `package.json`:
```json
"echarts": "^6.0.0",
"ng-echarts": "^3.0.1"
```

## 2. Pasos para usar ECharts

### 2.1. Importar en tu componente
```typescript
import { NgxEchartsDirective } from 'ngx-echarts';
import * as echarts from 'echarts';
```

### 2.2. Agregar la directiva al template
```html
<div echarts [options]="chartOptions" [style.width]="'100%'" [style.height]="'600px'"></div>
```

### 2.3. Definir opciones del gráfico en el componente
```typescript
export class MyComponent {
  chartOptions: any;

  ngOnInit() {
    this.chartOptions = {
      tooltip: { /* configuración */ },
      grid: { /* layout */ },
      xAxis: { type: 'category', data: ['Mon', 'Tue', 'Wed'] },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar', // 'line', 'scatter', 'custom', etc.
        data: [120, 200, 150]
      }]
    };
  }
}
```

## 3. Para tu Gantt Chart

**Ya lo implementé para ti:**
- Archivo: [planning/planning.component.ts](../../planning/planning.component.ts)
- Acceso: `http://localhost:4200/planning`

**Características incluidas:**
- ✅ Gráfico tipo Gantt personalizado
- ✅ Muestra tareas por equipo en el eje Y
- ✅ Tiempo en el eje X
- ✅ Tooltips interactivos
- ✅ Colores automáticos por tarea
- ✅ Datos en tiempo real desde tu API

## 4. Tipos de gráficos con ECharts
ECharts soporta muchos tipos:
- `bar` / `line` / `scatter` - Básicos
- `candlestick` - Finanzas
- `heatmap` - Mapas de calor
- `tree` / `treemap` - Jerarquías
- `gauge` - Medidores
- `custom` - **Personalizados (como tu Gantt)**
- `parallel` - Coordenadas paralelas
- `sunburst` - Gráficos solares

## 5. Documentación oficial
- **ECharts:** https://echarts.apache.org/en/index.html
- **ng-echarts:** https://xieziyu.github.io/ngx-echarts/
- **Ejemplos Gantt:** https://echarts.apache.org/examples/en/editor.html?c=custom-gantt-chart

## 6. Tips importantes

### Agregar responsividad
```typescript
@ViewChild('chart') chart!: NgxEchartsDirective;

@HostListener('window:resize')
onWindowResize() {
  this.chart.resize();
}
```

### Actualizar datos dinámicamente
```typescript
// Esto actualiza el gráfico automáticamente
this.chartOptions = {
  ...this.chartOptions,
  series: [{ ...newData }]
};
```

### Eventos interactivos
```typescript
this.chartOptions = {
  ...options,
  series: [{
    ...config,
    itemStyle: {
      color: '#FF6B6B'
    }
  }]
};

this.chart.getEchartsInstance().on('click', (params: any) => {
  console.log('Hiciste click en:', params);
});
```

---

**¿Necesitas personalizar más?** Solo modifica `planning.component.ts` y la sección `this.chartOptions`.
