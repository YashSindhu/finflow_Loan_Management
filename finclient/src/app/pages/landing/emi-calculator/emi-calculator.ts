import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-emi-calculator',
  standalone: true,
  imports: [FormsModule, DecimalPipe],
  templateUrl: './emi-calculator.html',
  styleUrl: './emi-calculator.css'
})
export class EmiCalculatorComponent {
  loanAmount = 500000;
  interestRate = 10;
  tenureMonths = 36;

  get emi(): number {
    const p = this.loanAmount;
    const r = this.interestRate / 12 / 100; // monthly rate
    const n = this.tenureMonths;
    if (r === 0) return p / n;
    return (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
  }

  get totalAmount(): number {
    return this.emi * this.tenureMonths;
  }

  get totalInterest(): number {
    return this.totalAmount - this.loanAmount;
  }
}
