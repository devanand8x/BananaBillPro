import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from './card';

describe('Card Components', () => {
    it('renders Card with content', () => {
        render(
            <Card>
                <div>Card content</div>
            </Card>
        );
        expect(screen.getByText('Card content')).toBeInTheDocument();
    });

    it('renders CardHeader with title and description', () => {
        render(
            <Card>
                <CardHeader>
                    <CardTitle>Card Title</CardTitle>
                    <CardDescription>Card Description</CardDescription>
                </CardHeader>
            </Card>
        );

        expect(screen.getByText('Card Title')).toBeInTheDocument();
        expect(screen.getByText('Card Description')).toBeInTheDocument();
    });

    it('renders CardContent', () => {
        render(
            <Card>
                <CardContent>
                    Main content here
                </CardContent>
            </Card>
        );
        expect(screen.getByText('Main content here')).toBeInTheDocument();
    });

    it('renders CardFooter', () => {
        render(
            <Card>
                <CardFooter>
                    Footer content
                </CardFooter>
            </Card>
        );
        expect(screen.getByText('Footer content')).toBeInTheDocument();
    });

    it('renders complete card structure', () => {
        render(
            <Card>
                <CardHeader>
                    <CardTitle>Test Card</CardTitle>
                    <CardDescription>Test Description</CardDescription>
                </CardHeader>
                <CardContent>
                    <p>Card body</p>
                </CardContent>
                <CardFooter>
                    <button>Action</button>
                </CardFooter>
            </Card>
        );

        expect(screen.getByText('Test Card')).toBeInTheDocument();
        expect(screen.getByText('Test Description')).toBeInTheDocument();
        expect(screen.getByText('Card body')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Action' })).toBeInTheDocument();
    });
});
