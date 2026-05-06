import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EmiCalculatorComponent } from './emi-calculator/emi-calculator';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink, EmiCalculatorComponent],
  templateUrl: './landing.html',
  styleUrl: './landing.css'
})
export class LandingComponent {}
